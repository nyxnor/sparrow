package com.sparrowwallet.sparrow.event;

import com.sparrowwallet.drongo.protocol.BlockHeader;

import java.util.List;

public class ConnectionEvent {
    private final List<String> serverVersion;
    private final String serverBanner;
    private final int blockHeight;
    private final BlockHeader blockHeader;

    public ConnectionEvent(List<String> serverVersion, String serverBanner, int blockHeight, BlockHeader blockHeader) {
        this.serverVersion = serverVersion;
        this.serverBanner = serverBanner;
        this.blockHeight = blockHeight;
        this.blockHeader = blockHeader;
    }

    public List<String> getServerVersion() {
        return serverVersion;
    }

    public String getServerBanner() {
        return serverBanner;
    }

    public int getBlockHeight() {
        return blockHeight;
    }

    public BlockHeader getBlockHeader() {
        return blockHeader;
    }
}