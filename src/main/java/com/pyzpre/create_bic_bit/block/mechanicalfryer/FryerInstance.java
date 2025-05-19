package com.pyzpre.create_bic_bit.block.mechanicalfryer;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.core.materials.oriented.OrientedData;
import com.mojang.math.Quaternion;
import com.pyzpre.create_bic_bit.index.PartialsRegistry;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.ShaftInstance;
import com.simibubi.create.content.kinetics.base.flwdata.RotatingData;
import com.simibubi.create.foundation.render.AllMaterialSpecs;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.core.Direction;

public class FryerInstance extends ShaftInstance<MechanicalFryerEntity> implements DynamicInstance {

    private final RotatingData fryerHead;
    private final OrientedData fryerPole;
    private final MechanicalFryerEntity fryer;

    public FryerInstance(MaterialManager materialManager, MechanicalFryerEntity blockEntity) {
        super(materialManager, blockEntity);
        this.fryer = blockEntity;

        fryerHead = materialManager.defaultCutout()
                .material(AllMaterialSpecs.ROTATING)
                .getModel(PartialsRegistry.MECHANICAL_FRYER_HEAD, blockState)
                .createInstance();

        fryerPole = getOrientedMaterial()
                .getModel(AllPartialModels.MECHANICAL_MIXER_POLE, blockState)
                .createInstance();

        float renderedHeadOffset = getRenderedHeadOffset();

        // Get the block's facing direction and apply rotation
        Direction facing = blockEntity.getBlockState().getValue(MechanicalFryer.HORIZONTAL_FACING);
        Quaternion rotation = getQuaternionForFacing(facing);

        transformPole(renderedHeadOffset, rotation);
        transformHead(renderedHeadOffset, rotation);
    }

    @Override
    public void beginFrame() {
        float renderedHeadOffset = getRenderedHeadOffset();

        // Get the block's facing direction and apply rotation
        Direction facing = blockEntity.getBlockState().getValue(MechanicalFryer.HORIZONTAL_FACING);
        Quaternion rotation = getQuaternionForFacing(facing);

        transformPole(renderedHeadOffset, rotation);
        transformHead(renderedHeadOffset, rotation);
    }

    private void transformHead(float renderedHeadOffset, Quaternion rotation) {
        fryerHead.setPosition(getInstancePosition())
                .nudge(0, -renderedHeadOffset, 0);
    }

    private void transformPole(float renderedHeadOffset, Quaternion rotation) {
        fryerPole.setPosition(getInstancePosition())
                .nudge(0, -renderedHeadOffset, 0)
                .setRotation(rotation);  // Apply rotation with quaternion
    }

    private float getRenderedHeadOffset() {
        return fryer.getRenderedHeadOffset(AnimationTickHolder.getPartialTicks());
    }


    private Quaternion getQuaternionForFacing(Direction facing) {
        // Create a quaternion for the Y-axis rotation based on the block facing
        switch (facing) {
            case NORTH:
                return new Quaternion(0, 0, 0, true); // No rotation
            case SOUTH:
                return new Quaternion(0, 180, 0, true); // 180 degrees
            case WEST:
                return new Quaternion(0, 90, 0, true); // 90 degrees
            case EAST:
                return new Quaternion(0, -90, 0, true); // -90 degrees
            default:
                return new Quaternion(0, 0, 0, true); // Default to no rotation
        }
    }

    @Override
    public void updateLight() {
        super.updateLight();
        relight(pos.below(), fryerHead);
        relight(pos, fryerPole);
    }

    @Override
    public void remove() {
        super.remove();
        fryerHead.delete();
        fryerPole.delete();
    }
}