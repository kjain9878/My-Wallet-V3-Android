package piuk.blockchain.androidcore.data.metadata

import info.blockchain.wallet.exceptions.InvalidCredentialsException
import info.blockchain.wallet.metadata.Metadata
import info.blockchain.wallet.metadata.MetadataDerivation
import info.blockchain.wallet.metadata.MetadataInteractor
import info.blockchain.wallet.metadata.MetadataNodeFactory
import info.blockchain.wallet.metadata.data.RemoteMetadataNodes
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import piuk.blockchain.androidcore.data.payload.PayloadDataManager
import piuk.blockchain.androidcore.utils.extensions.then

/**
 * Manages metadata nodes/keys derived from a user's wallet credentials.
 * This helps to avoid repeatedly asking user for second password.
 *
 * There are currently 2 nodes/keys (serialized privB58):
 * sharedMetadataNode   - used for inter-wallet communication
 * metadataNode         - used for storage
 *
 * The above nodes/keys can be derived from a user's master private key.
 * After these keys have been derived we store them on the metadata service with a node/key
 * derived from 'guid + sharedkey + wallet password'. This will allow us to retrieve these derived
 * keys with just a user's credentials and not derive them again.
 *
 */
class MetadataManager(
    private val payloadDataManager: PayloadDataManager,
    private val metadataInteractor: MetadataInteractor,
    private val metadataDerivation: MetadataDerivation
) {
    private val credentials: MetadataCredentials
        get() = payloadDataManager.metadataCredentials ?: throw IllegalStateException("Wallet not initialised")

    private var _metadataNodeFactory: MetadataNodeFactory? = null

    private val metadataNodeFactory: MetadataNodeFactory
        get() = _metadataNodeFactory?.let {
            it
        } ?: MetadataNodeFactory(credentials.guid,
            credentials.sharedKey,
            credentials.password,
            metadataDerivation).also {
            _metadataNodeFactory = it
        }

    fun attemptMetadataSetup() = initMetadataNodes()

    fun decryptAndSetupMetadata(
        secondPassword: String
    ): Completable {
        payloadDataManager.decryptHDWallet(secondPassword)
        return generateNodes()
            .then { initMetadataNodes() }
    }

    fun buildMetadata(metadataType: Int): Metadata =
        metadataNodeFactory.metadataNode?.let {
            Metadata.newInstance(metaDataHDNode = it, type = metadataType, metadataDerivation = metadataDerivation)
        } ?: throw IllegalStateException("Metadata node is null")

    fun fetchMetadata(metadataType: Int): Maybe<String> =
        metadataNodeFactory.metadataNode?.let {
            metadataInteractor.loadRemoteMetadata(Metadata.newInstance(metaDataHDNode = it,
                type = metadataType,
                metadataDerivation = metadataDerivation))
        } ?: Maybe.error(IllegalStateException("Metadata node is null"))

    fun saveToMetadata(data: String, metadataType: Int): Completable =
        metadataNodeFactory.metadataNode?.let {
            metadataInteractor.putMetadata(data,
                Metadata.newInstance(metaDataHDNode = it, type = metadataType, metadataDerivation = metadataDerivation))
        } ?: Completable.error(IllegalStateException("Metadata node is null"))

    /**
     * Loads or derives the stored nodes/keys from the metadata service.
     *
     * @throws InvalidCredentialsException If nodes/keys cannot be derived because wallet is double encrypted
     */
    private fun initMetadataNodes(): Completable =
        loadNodes().map { loaded ->
            if (!loaded) {
                if (payloadDataManager.isDoubleEncrypted) {
                    throw InvalidCredentialsException("Unable to derive metadata keys, payload is double encrypted")
                } else {
                    true
                }
            } else {
                false
            }
        }.flatMapCompletable { needsGeneration ->
            if (needsGeneration) {
                generateNodes()
            } else {
                Completable.complete()
            }
        }.subscribeOn(Schedulers.io())

    /**
     * Loads the metadata nodes from the metadata service. If this fails, the function returns false
     * and they must be generated and saved using this#generateNodes(String). This allows us
     * to generate and prompt for a second password only once.
     *
     * @return Returns true if the metadata nodes can be loaded from the service
     * @throws Exception Can throw an Exception if there's an issue with the credentials or network
     */
    private fun loadNodes(): Single<Boolean> =
        metadataInteractor.loadRemoteMetadata(metadataNodeFactory.secondPwNode)
            .map { metadata -> metadataNodeFactory.initNodes(RemoteMetadataNodes.fromJson(metadata)) }
            .defaultIfEmpty(false)
            .onErrorReturn { false }
            .toSingle()

    fun reset() {
        _metadataNodeFactory = null
    }

    /**
     * Generates the nodes for the shared metadata service and saves them on the service. Takes an
     * optional second password if set by the user. this#loadNodes(String, String, String)
     * must be called first to avoid a {@link NullPointerException}.
     *
     * @param secondPassword An optional second password, if applicable
     * @throws Exception Can throw a {@link DecryptionException} if the second password is wrong, or
     *                   a generic Exception if saving the nodes fails
     */
    private fun generateNodes(): Completable {
        val remoteMetadataNodes = metadataNodeFactory.remoteMetadataHdNodes(payloadDataManager.masterKey)
        return metadataInteractor.putMetadata(remoteMetadataNodes.toJson(), metadataNodeFactory.secondPwNode)
            .doOnComplete {
                metadataNodeFactory.initNodes(remoteMetadataNodes)
            }
    }

    companion object {
        const val METADATA_TYPE_EXCHANGE = 3
        const val METADATA_TYPE_SHAPE_SHIFT_EXTERNAL = 6
    }
}