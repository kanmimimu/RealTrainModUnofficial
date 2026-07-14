package jp.ngt.rtm.modelpack;

import com.portofino.realtrainmodunofficial.RealTrainModUnofficial;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

/**
 * 本家 jp.ngt.rtm.modelpack.ScriptExecuter 相当。
 *
 * <p>サーバースクリプト (serverScriptPath) は毎 tick
 * {@code onUpdate(entity, scriptExecuter)} として呼ばれ、第 2 引数にこれが渡る。
 * スクリプトはここから<b>バニラのコマンドを実行</b>できる。
 *
 * <p>本家は ICommandSender を実装していたが、1.21 のコマンドは
 * {@link CommandSourceStack} を取るので、実行のたびに組み立てる。
 * 権限レベルは本家と同じ 2 (コマンドブロック相当)、出力は捨てる。
 */
public class ScriptExecuter {

    /** 本家 count: スクリプトから経過 tick として読まれる。 */
    public long count;

    private final ServerLevel level;
    private final Vec3 pos;
    private final String name;

    public ScriptExecuter(ServerLevel level, Vec3 pos, String name) {
        this.level = level;
        this.pos = pos;
        this.name = name;
    }

    /**
     * スクリプトから呼ばれる。バニラコマンドを実行する。
     * 例: {@code scriptExecuter.execCommand("setblock 100 64 100 redstone_block")}
     */
    public void execCommand(String command) {
        if (command == null || command.isBlank() || this.level == null) {
            return;
        }
        MinecraftServer server = this.level.getServer();
        if (server == null) {
            return;
        }
        try {
            CommandSourceStack source = new CommandSourceStack(
                    CommandSource.NULL,          //出力は捨てる (本家も結果を見ない)
                    this.pos,
                    Vec2.ZERO,
                    this.level,
                    2,                           //本家 func_70003_b: permLevel <= 2
                    this.name,
                    Component.literal(this.name),
                    server,
                    null);
            server.getCommands().performPrefixedCommand(source, command);
        } catch (Throwable t) {
            RealTrainModUnofficial.LOGGER.warn("[serverScript] コマンド実行に失敗: {}", command, t);
        }
    }

    /** 本家 func_70005_c_ = getCommandSenderName */
    public String func_70005_c_() {
        return this.name;
    }

    public String getName() {
        return this.name;
    }

    /** 本家 func_130014_f_ = getEntityWorld */
    public jp.ngt.mccompat.WorldCompat func_130014_f_() {
        return new jp.ngt.mccompat.WorldCompat(this.level);
    }

    public long getCount() {
        return this.count;
    }
}
