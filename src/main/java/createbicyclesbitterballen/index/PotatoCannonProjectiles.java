package createbicyclesbitterballen.index;

import com.simibubi.create.Create;
import com.simibubi.create.content.equipment.potatoCannon.PotatoCannonProjectileType;
import net.minecraft.world.phys.EntityHitResult;

import java.util.function.Predicate;

public class PotatoCannonProjectiles{

    public static final PotatoCannonProjectileType
            BITTERBALLEN = create("bitterballen")
            .damage(4)
            .reloadTicks(12)
            .velocity(1.25f)
            .knockback(0.4f)
            .renderTowardMotion(140, 1)
            .soundPitch(1.5f)
            .splitInto(3)
            .preEntityHit(setFire(3))
            .registerAndAssign(CreateBicBitModItems.BITTERBALLEN.get()),
            KROKET = create("kroket")
                    .damage(5)
                    .reloadTicks(15)
                    .velocity(1.45f)
                    .knockback(0.4f)
                    .renderTowardMotion(140, 1)
                    .soundPitch(1.5f)
                    .preEntityHit(setFire(3))
                    .registerAndAssign(CreateBicBitModItems.KROKET.get()),
            FRIKANDEL = create("frikandel")
                    .damage(4)
                    .reloadTicks(10)
                    .velocity(1.45f)
                    .knockback(0.3f)
                    .renderTowardMotion(140, 1)
                    .soundPitch(1.5f)
                    .registerAndAssign(CreateBicBitModItems.FRIKANDEL.get()),
            OLIEBOLLEN = create("oliebollen")
            .damage(4)
            .reloadTicks(10)
            .velocity(1.25f)
            .knockback(0.4f)
            .renderTowardMotion(140, 1)
            .soundPitch(1.5f)
                    .splitInto(3)
            .registerAndAssign(CreateBicBitModItems.OLIEBOLLEN.get());


    public static void register() {

    }

    private static PotatoCannonProjectileType.Builder create(String name) {
        return new PotatoCannonProjectileType.Builder(Create.asResource(name));
    }
    private static Predicate<EntityHitResult> setFire(int seconds) {
        return ray -> {
            ray.getEntity()
                    .setSecondsOnFire(seconds);
            return false;
        };
    }

}
