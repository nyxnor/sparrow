package com.sparrowwallet.sparrow.wallet;

import com.samourai.whirlpool.client.mix.listener.MixFailReason;
import com.samourai.whirlpool.client.mix.listener.MixStep;
import com.samourai.whirlpool.client.wallet.beans.MixProgress;
import com.samourai.whirlpool.protocol.beans.Utxo;
import com.sparrowwallet.drongo.address.Address;
import com.sparrowwallet.drongo.wallet.*;
import com.sparrowwallet.sparrow.AppServices;
import com.sparrowwallet.sparrow.whirlpool.Whirlpool;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class UtxoEntry extends HashIndexEntry {
    private final WalletNode node;

    public UtxoEntry(Wallet wallet, BlockTransactionHashIndex hashIndex, Type type, WalletNode node) {
        super(wallet, hashIndex, type, node.getKeyPurpose());
        this.node = node;
    }

    @Override
    public ObservableList<Entry> getChildren() {
        return FXCollections.emptyObservableList();
    }

    @Override
    public String getDescription() {
        return getHashIndex().getHash().toString().substring(0, 8) + "..:" + getHashIndex().getIndex();
    }

    @Override
    public boolean isSpent() {
        return false;
    }

    public boolean isMixing() {
        return mixStatusProperty != null && ((mixStatusProperty.get().getMixProgress() != null && mixStatusProperty.get().getMixProgress().getMixStep() != MixStep.FAIL) || mixStatusProperty.get().getNextMixUtxo() != null);
    }

    public Address getAddress() {
        return getWallet().getAddress(node);
    }

    public WalletNode getNode() {
        return node;
    }

    public String getOutputDescriptor() {
        return getWallet().getOutputDescriptor(node);
    }

    /**
     * Defines whether this utxo shares it's address with another utxo in the wallet
     */
    private ObjectProperty<AddressStatus> addressStatusProperty;

    public final void setDuplicateAddress(boolean value) {
        addressStatusProperty().set(new AddressStatus(value));
    }

    public final boolean isDuplicateAddress() {
        return addressStatusProperty != null && addressStatusProperty.get().isDuplicate();
    }

    public final ObjectProperty<AddressStatus> addressStatusProperty() {
        if(addressStatusProperty == null) {
            addressStatusProperty = new SimpleObjectProperty<>(UtxoEntry.this, "addressStatus", new AddressStatus(false));
        }

        return addressStatusProperty;
    }

    public class AddressStatus {
        private final boolean duplicate;

        public AddressStatus(boolean duplicate) {
            this.duplicate = duplicate;
        }

        public UtxoEntry getUtxoEntry() {
            return UtxoEntry.this;
        }

        public Address getAddress() {
            return UtxoEntry.this.getAddress();
        }

        public boolean isDuplicate() {
            return duplicate;
        }
    }

    /**
     * Contains the mix status of this utxo, if available
     */
    private ObjectProperty<MixStatus> mixStatusProperty;

    public void setMixProgress(MixProgress mixProgress) {
        mixStatusProperty().set(new MixStatus(mixProgress));
    }

    public void setMixFailReason(MixFailReason mixFailReason) {
        mixStatusProperty().set(new MixStatus(mixFailReason));
    }

    public void setNextMixUtxo(Utxo nextMixUtxo) {
        mixStatusProperty().set(new MixStatus(nextMixUtxo));
    }

    public final MixStatus getMixStatus() {
        return mixStatusProperty == null ? null : mixStatusProperty.get();
    }

    public final ObjectProperty<MixStatus> mixStatusProperty() {
        if(mixStatusProperty == null) {
            mixStatusProperty = new SimpleObjectProperty<>(UtxoEntry.this, "mixStatus", null);
        }

        return mixStatusProperty;
    }

    public class MixStatus {
        private MixProgress mixProgress;
        private Utxo nextMixUtxo;
        private MixFailReason mixFailReason;

        public MixStatus(MixProgress mixProgress) {
            this.mixProgress = mixProgress;
        }

        public MixStatus(Utxo nextMixUtxo) {
            this.nextMixUtxo = nextMixUtxo;
        }

        public MixStatus(MixFailReason mixFailReason) {
            this.mixFailReason = mixFailReason;
        }

        public UtxoEntry getUtxoEntry() {
            return UtxoEntry.this;
        }

        public UtxoMixData getUtxoMixData() {
            Wallet wallet = getUtxoEntry().getWallet().getMasterWallet();
            if(wallet.getUtxoMixData(getHashIndex()) != null) {
                return wallet.getUtxoMixData(getHashIndex());
            }

            Whirlpool whirlpool = AppServices.getWhirlpoolServices().getWhirlpool(wallet);
            if(whirlpool != null) {
                UtxoMixData utxoMixData = whirlpool.getMixData(getHashIndex());
                if(utxoMixData != null) {
                    return utxoMixData;
                }
            }

            return new UtxoMixData(getUtxoEntry().getWallet().getStandardAccountType() == StandardAccount.WHIRLPOOL_POSTMIX ? 1 : 0, null);
        }

        public int getMixesDone() {
            return getUtxoMixData().getMixesDone();
        }

        public MixProgress getMixProgress() {
            return mixProgress;
        }

        public Utxo getNextMixUtxo() {
            return nextMixUtxo;
        }

        public MixFailReason getMixFailReason() {
            return mixFailReason;
        }
    }
}
