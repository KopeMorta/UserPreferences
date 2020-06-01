package com.kopemorta.userpreferences;

public final class DefaultRegisterOptions {

    public static final RegisterOption SAVE_ON_DISK = new SaveOnDisk();

    public static final RegisterOption LOAD_FROM_DISK = new LoadFromDisk();

    public static final RegisterOption SINGLE_SAVE_ON_DISK = new SingleSaveOnDisk();

    public static final RegisterOption SINGLE_LOAD_FROM_DISK = new SingleLoadFromDisk();


    private static class SaveOnDisk implements RegisterOption {
        @Override
        public boolean repeatable() {
            return true;
        }
    }

    private static class LoadFromDisk implements RegisterOption {
        @Override
        public boolean repeatable() {
            return true;
        }
    }

    private final static class SingleSaveOnDisk extends SaveOnDisk {
        @Override
        public boolean repeatable() {
            return false;
        }
    }

    private final static class SingleLoadFromDisk extends LoadFromDisk {
        @Override
        public boolean repeatable() {
            return false;
        }
    }
}
