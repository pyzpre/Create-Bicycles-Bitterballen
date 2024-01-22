package pyzpre.createbicyclesbitterballen.block.mechanicalfryer;


import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.core.materials.oriented.OrientedData;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.ShaftInstance;
import com.simibubi.create.content.kinetics.base.flwdata.RotatingData;
import com.simibubi.create.foundation.render.AllMaterialSpecs;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import pyzpre.createbicyclesbitterballen.index.PartialsRegistry;
import net.minecraft.core.Direction;
public class FryerInstance extends ShaftInstance<MechanicalFryerEntity> implements DynamicInstance {


    private final RotatingData mixerHead;
    private final OrientedData mixerPole;
    private final MechanicalFryerEntity mixer;
    public FryerInstance(MaterialManager materialManager, MechanicalFryerEntity blockEntity) {
        super(materialManager, blockEntity);
        this.mixer = blockEntity;

        mixerHead = materialManager.defaultCutout()
                .material(AllMaterialSpecs.ROTATING)
                .getModel(PartialsRegistry.MECHANICAL_FRYER_HEAD, blockState)
                .createInstance();

        mixerHead.setRotationAxis(Direction.Axis.Y);

        mixerPole = getOrientedMaterial()
                .getModel(AllPartialModels.MECHANICAL_MIXER_POLE, blockState)
                .createInstance();


        float renderedHeadOffset = getRenderedHeadOffset();

        transformPole(renderedHeadOffset);
        transformHead(renderedHeadOffset);
    }


    @Override
    public void beginFrame() {

        float renderedHeadOffset = getRenderedHeadOffset();

        transformPole(renderedHeadOffset);
        transformHead(renderedHeadOffset);
    }

    private void transformHead(float renderedHeadOffset) {


        mixerHead.setPosition(getInstancePosition())
                .nudge(0, -renderedHeadOffset, 0);
    }

    private void transformPole(float renderedHeadOffset) {
        mixerPole.setPosition(getInstancePosition())
                .nudge(0, -renderedHeadOffset, 0);
    }

    private float getRenderedHeadOffset() {
        return mixer.getRenderedHeadOffset(AnimationTickHolder.getPartialTicks());
    }

    @Override
    public void updateLight() {
        super.updateLight();

        relight(pos.below(), mixerHead);
        relight(pos, mixerPole);
    }

    @Override
    public void remove() {
        super.remove();
        mixerHead.delete();
        mixerPole.delete();
    }
}