package me.josielcm.event.manager.games.giantgift;

import lombok.Getter;

public enum GiftCapacities {
    RD_1(36, 4),
    RD_2(33, 3),
    RD_3(29, 4),
    RD_4(26, 3),
    RD_5(23, 3),
    RD_6(20, 3);

    @Getter
    private final int pass;

    @Getter
    private final int elimination;

    GiftCapacities(int pass, int elimination) {
        this.pass = pass;
        this.elimination = elimination;
    }
    
}
