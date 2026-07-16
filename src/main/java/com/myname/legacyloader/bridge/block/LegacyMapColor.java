package com.myname.legacyloader.bridge.block;

public class LegacyMapColor {
    public static final LegacyMapColor[] mapColorArray = new LegacyMapColor[64];
    public static final LegacyMapColor[] field_76281_a = mapColorArray;

    public static final LegacyMapColor airColor = new LegacyMapColor(0, 0);
    public static final LegacyMapColor grassColor = new LegacyMapColor(1, 8368696);
    public static final LegacyMapColor sandColor = new LegacyMapColor(2, 16247203);
    public static final LegacyMapColor clothColor = new LegacyMapColor(3, 10987431);
    public static final LegacyMapColor tntColor = new LegacyMapColor(4, 16711680);
    public static final LegacyMapColor iceColor = new LegacyMapColor(5, 10526975);
    public static final LegacyMapColor ironColor = new LegacyMapColor(6, 10987431);
    public static final LegacyMapColor foliageColor = new LegacyMapColor(7, 31744);
    public static final LegacyMapColor snowColor = new LegacyMapColor(8, 16777215);
    public static final LegacyMapColor clayColor = new LegacyMapColor(9, 10791096);
    public static final LegacyMapColor dirtColor = new LegacyMapColor(10, 12020271);
    public static final LegacyMapColor stoneColor = new LegacyMapColor(11, 7368816);
    public static final LegacyMapColor waterColor = new LegacyMapColor(12, 4210943);
    public static final LegacyMapColor woodColor = new LegacyMapColor(13, 6837042);
    public static final LegacyMapColor quartzColor = new LegacyMapColor(14, 16776437);
    public static final LegacyMapColor adobeColor = new LegacyMapColor(15, 14188339);
    public static final LegacyMapColor magentaColor = new LegacyMapColor(16, 11685080);
    public static final LegacyMapColor lightBlueColor = new LegacyMapColor(17, 6724056);
    public static final LegacyMapColor yellowColor = new LegacyMapColor(18, 15066419);
    public static final LegacyMapColor limeColor = new LegacyMapColor(19, 8375321);
    public static final LegacyMapColor pinkColor = new LegacyMapColor(20, 15892389);
    public static final LegacyMapColor grayColor = new LegacyMapColor(21, 5000268);
    public static final LegacyMapColor silverColor = new LegacyMapColor(22, 10066329);
    public static final LegacyMapColor cyanColor = new LegacyMapColor(23, 5013401);
    public static final LegacyMapColor purpleColor = new LegacyMapColor(24, 8339378);
    public static final LegacyMapColor blueColor = new LegacyMapColor(25, 3361970);
    public static final LegacyMapColor brownColor = new LegacyMapColor(26, 6704179);
    public static final LegacyMapColor greenColor = new LegacyMapColor(27, 6717235);
    public static final LegacyMapColor redColor = new LegacyMapColor(28, 10040115);
    public static final LegacyMapColor blackColor = new LegacyMapColor(29, 1644825);
    public static final LegacyMapColor goldColor = new LegacyMapColor(30, 16445005);
    public static final LegacyMapColor diamondColor = new LegacyMapColor(31, 6085589);
    public static final LegacyMapColor lapisColor = new LegacyMapColor(32, 4882687);
    public static final LegacyMapColor emeraldColor = new LegacyMapColor(33, 55610);
    public static final LegacyMapColor obsidianColor = new LegacyMapColor(34, 1381407);
    public static final LegacyMapColor netherrackColor = new LegacyMapColor(35, 7340544);

    public static final LegacyMapColor field_151660_b = airColor;
    public static final LegacyMapColor field_151661_c = grassColor;
    public static final LegacyMapColor field_151658_d = sandColor;
    public static final LegacyMapColor field_151659_e = clothColor;
    public static final LegacyMapColor field_151656_f = tntColor;
    public static final LegacyMapColor field_151657_g = iceColor;
    public static final LegacyMapColor field_151668_h = ironColor;
    public static final LegacyMapColor field_151669_i = foliageColor;
    public static final LegacyMapColor field_151666_j = snowColor;
    public static final LegacyMapColor field_151667_k = clayColor;
    public static final LegacyMapColor field_151664_l = dirtColor;
    public static final LegacyMapColor field_151665_m = stoneColor;
    public static final LegacyMapColor field_151662_n = waterColor;
    public static final LegacyMapColor field_151663_o = woodColor;
    public static final LegacyMapColor field_151676_p = quartzColor;
    public static final LegacyMapColor field_151675_q = adobeColor;
    public static final LegacyMapColor field_151674_r = magentaColor;
    public static final LegacyMapColor field_151673_s = lightBlueColor;
    public static final LegacyMapColor field_151672_t = yellowColor;
    public static final LegacyMapColor field_151671_u = limeColor;
    public static final LegacyMapColor field_151670_v = pinkColor;
    public static final LegacyMapColor field_151684_w = grayColor;
    public static final LegacyMapColor field_151683_x = silverColor;
    public static final LegacyMapColor field_151682_y = cyanColor;
    public static final LegacyMapColor field_151681_z = purpleColor;
    public static final LegacyMapColor field_151649_A = blueColor;
    public static final LegacyMapColor field_151650_B = brownColor;
    public static final LegacyMapColor field_151651_C = greenColor;
    public static final LegacyMapColor field_151645_D = redColor;
    public static final LegacyMapColor field_151646_E = blackColor;
    public static final LegacyMapColor field_151647_F = goldColor;
    public static final LegacyMapColor field_151648_G = diamondColor;
    public static final LegacyMapColor field_151649_H = lapisColor;
    public static final LegacyMapColor field_151650_I = emeraldColor;
    public static final LegacyMapColor field_151651_J = obsidianColor;
    public static final LegacyMapColor field_151652_K = netherrackColor;
    public static final LegacyMapColor field_151673_t = yellowColor;

    public final int colorValue;
    public final int colorIndex;
    public final int field_76290_q;
    public final int field_76291_p;

    public LegacyMapColor(int color) {
        this(-1, color);
    }

    public LegacyMapColor(int index, int color) {
        this.colorIndex = index;
        this.colorValue = color;
        this.field_76290_q = color;
        this.field_76291_p = index;
        if (index >= 0 && index < mapColorArray.length) {
            mapColorArray[index] = this;
        }
    }

    public static LegacyMapColor getMapColorForBlockColored(int meta) {
        return switch (15 - (meta & 15)) {
            case 0 -> blackColor;
            case 1 -> redColor;
            case 2 -> greenColor;
            case 3 -> brownColor;
            case 4 -> blueColor;
            case 5 -> purpleColor;
            case 6 -> cyanColor;
            case 7 -> silverColor;
            case 8 -> grayColor;
            case 9 -> pinkColor;
            case 10 -> limeColor;
            case 11 -> yellowColor;
            case 12 -> lightBlueColor;
            case 13 -> magentaColor;
            case 14 -> adobeColor;
            case 15 -> snowColor;
            default -> airColor;
        };
    }

    public int func_151643_b(int shade) {
        int multiplier = switch (shade) {
            case 3 -> 135;
            case 2 -> 255;
            case 1 -> 220;
            default -> 180;
        };
        int r = (this.colorValue >> 16 & 255) * multiplier / 255;
        int g = (this.colorValue >> 8 & 255) * multiplier / 255;
        int b = (this.colorValue & 255) * multiplier / 255;
        return 0xFF000000 | r << 16 | g << 8 | b;
    }
}
