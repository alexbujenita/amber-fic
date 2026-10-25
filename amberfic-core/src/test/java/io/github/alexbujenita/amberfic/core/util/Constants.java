package io.github.alexbujenita.amberfic.core.util;

public final class Constants {

    public static final String EMPTY_SHA_256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

    public static final String ABC_SHA_256 = "b5d4045c3f466fa91fe2cc6abe79232a1a57cdf104f7a26e716e0a1e2789df78";

    public static final String EMPTY_SHA_512 =
            "cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce47d0d13c5d85f2b0ff8318d2877eec2f63b931bd47417a81a538327af927da3e";

    public static final String ABC_SHA_512 =
            "397118fdac8d83ad98813c50759c85b8c47565d8268bf10da483153b747a74743a58a90e85aa9f705ce6984ffc128db567489817e4092d050d8a1cc596ddc119";

    public static final String ADC_SHA_512 =
            "6cc40041ebedd74e50a92737bfcd4c03a3826ab4d09ff0076421479b36f5789de45c573a4e0cc45d99b3ad23db5a448ee5c5328cc1bd8334ebb7e5813744909e";

    private Constants() {
        throw new IllegalStateException("Constants class");
    }
}
