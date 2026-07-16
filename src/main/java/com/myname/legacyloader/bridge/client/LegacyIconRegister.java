package com.myname.legacyloader.bridge.client;

public interface LegacyIconRegister {

    // 1.7.10 API: registerIcon
    LegacyIcon registerIcon(String name);

    // 笘・ｿｽ蜉: SRG蜷・(func_94245_a) 縺ｮ繧ｨ繧､繝ｪ繧｢繧ｹ
    // Mod縺後％縺ｮ蜷榊燕縺ｧ蜻ｼ縺ｳ蜃ｺ縺励※繧・registerIcon 縺ｫ霆｢騾√☆繧・
    default LegacyIcon func_94245_a(String name) {
        return registerIcon(name);
    }
}