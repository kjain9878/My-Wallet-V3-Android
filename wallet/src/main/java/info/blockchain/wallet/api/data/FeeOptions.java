package info.blockchain.wallet.api.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import info.blockchain.balance.CryptoCurrency;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FeeOptions {

    @JsonProperty("gasLimit")
    private long gasLimit;

    @JsonProperty("regular")
    private long regularFee;

    @JsonProperty("priority")
    private long priorityFee;

    @JsonProperty("limits")
    private FeeLimits limits;

    /**
     * @return the default FeeOptions for XLM.
     */
    private static FeeOptions defaultForXlm() {
        final FeeOptions feeOptions = new FeeOptions();
        feeOptions.setPriorityFee(100);
        feeOptions.setRegularFee(100);
        return feeOptions;
    }

    /**
     * @return the default FeeOptions for Ethereum.
     */
    private static FeeOptions defaultForEth() {
        final FeeOptions feeOptions = new FeeOptions();
        feeOptions.setGasLimit(21000);
        feeOptions.setPriorityFee(23);
        feeOptions.setRegularFee(23);
        feeOptions.setLimits(new FeeLimits(23, 23));
        return feeOptions;
    }

    /**
     * @return the default FeeOptions for Bitcoin.
     */
    private static FeeOptions defaultForBtc() {
        final FeeOptions feeOptions = new FeeOptions();
        feeOptions.setPriorityFee(11);
        feeOptions.setRegularFee(5);
        feeOptions.setLimits(new FeeLimits(2, 16));
        return feeOptions;
    }

    private static FeeOptions defaultForBch() {
        final FeeOptions feeOptions = new FeeOptions();
        feeOptions.setRegularFee(4);
        feeOptions.setPriorityFee(4);
        return feeOptions;
    }

    /**
     * @param currency the currency
     * @return the default FeeOptions given a currency
     */
    public static FeeOptions defaultFee(CryptoCurrency currency) {
        FeeOptions feeOptions = null;
        switch (currency) {
            case BTC:
                feeOptions = defaultForBtc();
                break;
            case ETHER:
                feeOptions = defaultForEth();
                break;
            case BCH:
                feeOptions = defaultForBch();
                break;
            case XLM:
                feeOptions = defaultForXlm();
                break;
        }
        return feeOptions;
    }

    /**
     * Returns a "gasLimit" for Ethereum
     */
    public long getGasLimit() {
        return gasLimit;
    }

    /**
     * Returns a "regular" fee, which should result in a transaction being included in a block
     * within the next 4-6 hours. The fee is in Satoshis per byte.
     */
    public long getRegularFee() {
        return regularFee;
    }

    /**
     * Returns a "priority" fee, which should result in a transaction being included in a block in
     * an hour or so. The fee is in Satoshis per byte.
     */
    public long getPriorityFee() {
        return priorityFee;
    }

    /**
     * Returns a "priority" fee, which should result in a transaction being included in a block in
     * an hour or so. The fee is in Satoshis per byte.
     */
    public FeeLimits getLimits() {
        return limits;
    }

    public void setGasLimit(long gasLimit) {
        this.gasLimit = gasLimit;
    }

    public void setRegularFee(long regularFee) {
        this.regularFee = regularFee;
    }

    public void setPriorityFee(long priorityFee) {
        this.priorityFee = priorityFee;
    }

    public void setLimits(FeeLimits limits) {
        this.limits = limits;
    }
}
