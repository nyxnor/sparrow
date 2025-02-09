package com.sparrowwallet.sparrow.wallet;

import com.sparrowwallet.drongo.policy.PolicyType;
import com.sparrowwallet.drongo.wallet.MixConfig;
import com.sparrowwallet.drongo.wallet.StandardAccount;
import com.sparrowwallet.drongo.wallet.Wallet;
import com.sparrowwallet.sparrow.AppServices;
import com.sparrowwallet.sparrow.EventManager;
import com.sparrowwallet.sparrow.event.WalletMasterMixConfigChangedEvent;
import com.sparrowwallet.sparrow.whirlpool.Whirlpool;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MixToController implements Initializable {
    private static final Wallet NONE_WALLET = new Wallet("None");

    @FXML
    private ComboBox<Wallet> mixToWallets;

    @FXML
    private Spinner<Integer> minMixes;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void initializeView(Wallet wallet) {
        MixConfig mixConfig = wallet.getMasterMixConfig();

        List<Wallet> allWallets = new ArrayList<>();
        allWallets.add(NONE_WALLET);

        List<Wallet> destinationWallets = AppServices.get().getOpenWallets().keySet().stream().filter(openWallet -> openWallet.isValid()
                && openWallet != wallet && openWallet != wallet.getMasterWallet()
                && !StandardAccount.WHIRLPOOL_ACCOUNTS.contains(openWallet.getStandardAccountType())).collect(Collectors.toList());
        allWallets.addAll(destinationWallets);

        mixToWallets.setItems(FXCollections.observableList(allWallets));

        String mixToWalletId = null;
        try {
            mixToWalletId = AppServices.getWhirlpoolServices().getWhirlpoolMixToWalletId(mixConfig);
        } catch(NoSuchElementException e) {
            //ignore, mix to wallet is not open
        }

        if(mixToWalletId != null) {
            mixToWallets.setValue(AppServices.get().getWallet(mixToWalletId));
        } else {
            mixToWallets.setValue(NONE_WALLET);
        }

        mixToWallets.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue == NONE_WALLET) {
                mixConfig.setMixToWalletName(null);
                mixConfig.setMixToWalletFile(null);
            } else {
                mixConfig.setMixToWalletName(newValue.getName());
                mixConfig.setMixToWalletFile(AppServices.get().getOpenWallets().get(newValue).getWalletFile());
            }

            EventManager.get().post(new WalletMasterMixConfigChangedEvent(wallet));
        });

        int initialMinMixes = mixConfig.getMinMixes() == null ? Whirlpool.DEFAULT_MIXTO_MIN_MIXES : mixConfig.getMinMixes();
        minMixes.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10000, initialMinMixes));
        minMixes.valueProperty().addListener((observable, oldValue, newValue) -> {
            mixConfig.setMinMixes(newValue);
            EventManager.get().post(new WalletMasterMixConfigChangedEvent(wallet));
        });
    }
}
