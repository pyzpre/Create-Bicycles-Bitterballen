package com.pyzpre.createbitterballen.block.mechanicalfryer;

import com.pyzpre.createbitterballen.index.PartialsRegistry;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.ShaftVisual;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.OrientedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.minecraft.core.Direction;
import org.joml.Quaternionf;


public class FryerInstance extends ShaftVisual<MechanicalFryerEntity> implements SimpleDynamicVisual {

    private final OrientedInstance fryerHead;
    private final OrientedInstance fryerPole;
    private final MechanicalFryerEntity fryer;

    public FryerInstance(VisualizationContext context, MechanicalFryerEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);
        this.fryer = blockEntity;

        fryerHead = instancerProvider().instancer(InstanceTypes.ORIENTED, Models.partial(PartialsRegistry.MECHANICAL_FRYER_HEAD))
                .createInstance();

        fryerPole = instancerProvider().instancer(InstanceTypes.ORIENTED, Models.partial(AllPartialModels.MECHANICAL_MIXER_POLE))
                .createInstance();

        // Apply initial transformation
        transformModels(partialTick);
    }
    private void transformModels(float pt) {
        float renderedHeadOffset = getRenderedHeadOffset(pt);

        // Get the block's facing direction
        Direction facing = blockState.getValue(MechanicalFryer.HORIZONTAL_FACING);
        Quaternionf rotation = getQuaternionForFacing(facing);

        // Ensure both fryerHead and fryerPole move correctly
        fryerHead.position(getVisualPosition())
                .translatePosition(0, -renderedHeadOffset, 0)
                .setChanged();

        fryerPole.position(getVisualPosition())
                .translatePosition(0, -renderedHeadOffset, 0)
                .rotation(rotation)  // Apply rotation
                .setChanged();
    }

    // Method to get correct rotation for pole
    private Quaternionf getQuaternionForFacing(Direction facing) {
        switch (facing) {
            case NORTH:
                return new Quaternionf().rotateY(0);  // Default rotation
            case SOUTH:
                return new Quaternionf().rotateY((float) Math.PI);  // 180 degrees
            case WEST:
                return new Quaternionf().rotateY((float) Math.PI / 2);  // 90 degrees
            case EAST:
                return new Quaternionf().rotateY(-(float) Math.PI / 2); // -90 degrees
            default:
                return new Quaternionf();
        }
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        float pt = ctx.partialTick();
        transformModels(pt);
    }

    private float getRenderedHeadOffset(float pt) {
        return fryer.getRenderedHeadOffset(pt);
    }

    @Override
    public void updateLight(float partialTick) {
        super.updateLight(partialTick);
        relight(pos.below(), fryerHead);
        relight(pos, fryerPole);
    }

    @Override
    protected void _delete() {
        super._delete();
        fryerHead.delete();
        fryerPole.delete();
    }

    @Override
    public void collectCrumblingInstances(java.util.function.Consumer<dev.engine_room.flywheel.api.instance.Instance> consumer) {
        super.collectCrumblingInstances(consumer);
        consumer.accept(fryerHead);
        consumer.accept(fryerPole);
    }
}
