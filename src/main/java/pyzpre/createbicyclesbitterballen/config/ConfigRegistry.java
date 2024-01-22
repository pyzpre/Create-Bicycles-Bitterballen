package pyzpre.createbicyclesbitterballen.config;
import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigRegistry {
    public static final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SERVER_SPEC;


    static {
        SERVER_BUILDER.push("Server Configs");

        SERVER_BUILDER.pop();
        SERVER_SPEC = SERVER_BUILDER.build();
    }
}