package com.pyzpre.create_bic_bit.index;

import com.simibubi.create.Create;
import com.simibubi.create.content.equipment.potatoCannon.PotatoCannonProjectileType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;

import java.util.function.Predicate;

public class PotatoCannonProjectiles {

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
            .registerAndAssign(ItemRegistry.BITTERBALLEN.get()),
            KROKET = create("kroket")
                    .damage(5)
                    .reloadTicks(15)
                    .velocity(1.45f)
                    .knockback(0.4f)
                    .renderTowardMotion(140, 1)
                    .soundPitch(1.5f)
                    .preEntityHit(setFire(3))
                    .registerAndAssign(ItemRegistry.KROKET.get()),
            FRIKANDEL = create("frikandel")
                    .damage(4)
                    .reloadTicks(10)
                    .velocity(1.45f)
                    .knockback(0.3f)
                    .renderTowardMotion(140, 1)
                    .soundPitch(1.5f)
                    .registerAndAssign(ItemRegistry.FRIKANDEL.get()),
            KRUIDNOTEN = create("kruidnoten")
                    .damage(2)
                    .reloadTicks(5)
                    .velocity(1.25f)
                    .knockback(0.2f)
                    .renderTowardMotion(140, 1)
                    .soundPitch(1.5f)
                    .splitInto(2)
                    .registerAndAssign(ItemRegistry.KRUIDNOTEN.get()),
            OLIEBOLLEN = create("oliebollen")
            .damage(4)
            .reloadTicks(10)
            .velocity(1.25f)
            .knockback(0.4f)
            .renderTowardMotion(140, 1)
            .soundPitch(1.5f)
            .splitInto(3)
            .preEntityHit(oilUpTarget(200))
            .registerAndAssign(ItemRegistry.OLIEBOLLEN.get()),
            COATED_OLIEBOLLEN = create("coated_oliebollen")
                    .damage(4)
                    .reloadTicks(10)
                    .velocity(1.25f)
                    .knockback(0.4f)
                    .renderTowardMotion(140, 1)
                    .soundPitch(1.5f)
                    .splitInto(3)
                    .preEntityHit(oilUpTarget(200))
                    .registerAndAssign(ItemRegistry.COATED_OLIEBOLLEN.get());





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
    private static Predicate<EntityHitResult> oilUpTarget(int duration) {
        return ray -> {
            if (ray.getEntity() instanceof LivingEntity livingEntity) {
                livingEntity.addEffect(new MobEffectInstance(EffectRegistry.OILED_UP.get(), duration));
            }
            return false;
        };
    }

}
