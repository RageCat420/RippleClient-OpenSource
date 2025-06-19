package dev.ripple;

import dev.ripple.api.events.eventbus.EventBus;
import dev.ripple.api.utils.verification.VerificationManager;
import dev.ripple.core.impl.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.lang.invoke.MethodHandles;

public final class Ripple implements ModInitializer {

//                            _ooOoo_
//                           o8888888o
//                           88" . "88
//                           (| -_- |)
//                            O\ = /O
//                        ____/`---'\____
//                      .   ' \\| |// `.
//                       / \\||| : |||// \
//                     / _||||| -:- |||||- \
//                       | | \\\ - /// | |
//                     | \_| ''\---/'' | |
//                      \ .-\__ `-` ___/-. /
//                   ___`. .' /--.--\ `. . __
//                ."" '< `.___\_<|>_/___.' >'"".
//               | | : `- \`.;`\ _ /`;.`/ - ` : | |
//                 \ \ `-. \_ __\ /__ _/ .-` / /
//         ======`-.____`-.___\_____/___.-`____.-'======
//                            `=---='
//
//         .............................................
//                  佛祖保佑             永无BUG
//          佛曰:
//                  写字楼里写字间，写字间里程序员；
//                  程序人员写程序，又拿程序换酒钱。
//                  酒醒只在网上坐，酒醉还来网下眠；
//                  酒醉酒醒日复日，网上网下年复年。
//                  但愿老死电脑间，不愿鞠躬老板前；
//                  奔驰宝马贵者趣，公交自行程序员。
//                  别人笑我忒疯癫，我笑自己命太贱；
//                  不见满街漂亮妹，哪个归得程序员？

// 程序出Bug了？
// 　　　∩∩
// 　　（´･ω･）
// 　 ＿|　⊃／(＿＿_
// 　／ └-(＿＿＿／
// 　￣￣￣￣￣￣￣
// 算了反正不是我写的
// 　　 ⊂⌒／ヽ-、＿
// 　／⊂_/＿＿＿＿ ／
// 　￣￣￣￣￣￣￣
// 万一是我写的呢
// 　　　∩∩
// 　　（´･ω･）
// 　 ＿|　⊃／(＿＿_
// 　／ └-(＿＿＿／
// 　￣￣￣￣￣￣￣
// 算了反正改了一个又出三个
// 　　 ⊂⌒／ヽ-、＿
// 　／⊂_/＿＿＿＿ ／
// 　￣￣￣￣￣￣￣

    /**
     *                      江城子 . 程序员之歌
     *
     *                  十年生死两茫茫，写程序，到天亮。
     *                      千行代码，Bug何处藏。
     *                  纵使上线又怎样，朝令改，夕断肠。
     *
     *                  领导每天新想法，天天改，日日忙。
     *                      相顾无言，惟有泪千行。
     *                  每晚灯火阑珊处，夜难寐，加班狂。
     */

    @Override
    public void onInitialize() {
        load();
    }

    public static final String NAME = "Ripple";
    public static final String VERSION = "1.0";
    public static String PREFIX = ";";
    public static final EventBus EVENT_BUS = new EventBus();
    public static IRCManager IRC;
    public static HoleManager HOLE;
    public static PlayerManager PLAYER;
    public static TradeManager TRADE;
    public static XrayManager XRAY;
    public static ModuleManager MODULE;
    public static GuiManager GUI;
    public static ConfigManager CONFIG;
    public static RotationManager ROTATION;
    public static BreakManager BREAK;
    public static PopManager POP;
    public static FriendManager FRIEND;
    public static CommandManager COMMAND;
    public static TimerManager TIMER;
    public static ShaderManager SHADER;
    public static FPSManager FPS;
    public static ServerManager SERVER;
    public static ThreadManager THREAD;
    public static boolean loaded = false;

    public static void load() {
        EVENT_BUS.registerLambdaFactory((lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
        VerificationManager.client("Ripple");
        CONFIG = new ConfigManager();
        PREFIX = Ripple.CONFIG.getString("prefix", ";");
        IRC = new IRCManager();
        THREAD = new ThreadManager();
        HOLE = new HoleManager();
        MODULE = new ModuleManager();
        COMMAND = new CommandManager();
        GUI = new GuiManager();
        FRIEND = new FriendManager();
        XRAY = new XrayManager();
        TRADE = new TradeManager();
        ROTATION = new RotationManager();
        BREAK = new BreakManager();
        PLAYER = new PlayerManager();
        POP = new PopManager();
        TIMER = new TimerManager();
        SHADER = new ShaderManager();
        FPS = new FPSManager();
        SERVER = new ServerManager();

        CONFIG.loadSettings();

        System.out.println("[" + Ripple.NAME + "] loaded");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (loaded) {
                save();
            }
        }));
        loaded = true;
    }

    public static void unload() {
        loaded = false;
        System.out.println("[" + Ripple.NAME + "] Unloading..");
        EVENT_BUS.listenerMap.clear();
        ConfigManager.resetModule();
        System.out.println("[" + Ripple.NAME + "] Unloaded");
    }

    public static void save() {
        System.out.println("[" + Ripple.NAME + "] Saving");
        CONFIG.saveSettings();
        FRIEND.save();
        XRAY.save();
        TRADE.save();
        System.out.println("[" + Ripple.NAME + "] Saved");
    }
}
