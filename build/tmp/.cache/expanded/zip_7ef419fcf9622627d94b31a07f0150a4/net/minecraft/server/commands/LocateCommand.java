package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceOrTagLocationArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;

public class LocateCommand {
   private static final DynamicCommandExceptionType ERROR_STRUCTURE_NOT_FOUND = new DynamicCommandExceptionType((p_201831_) -> {
      return Component.translatable("commands.locate.structure.not_found", p_201831_);
   });
   private static final DynamicCommandExceptionType ERROR_STRUCTURE_INVALID = new DynamicCommandExceptionType((p_207534_) -> {
      return Component.translatable("commands.locate.structure.invalid", p_207534_);
   });
   private static final DynamicCommandExceptionType ERROR_BIOME_NOT_FOUND = new DynamicCommandExceptionType((p_214514_) -> {
      return Component.translatable("commands.locate.biome.not_found", p_214514_);
   });
   private static final DynamicCommandExceptionType ERROR_BIOME_INVALID = new DynamicCommandExceptionType((p_214512_) -> {
      return Component.translatable("commands.locate.biome.invalid", p_214512_);
   });
   private static final DynamicCommandExceptionType ERROR_POI_NOT_FOUND = new DynamicCommandExceptionType((p_214505_) -> {
      return Component.translatable("commands.locate.poi.not_found", p_214505_);
   });
   private static final DynamicCommandExceptionType ERROR_POI_INVALID = new DynamicCommandExceptionType((p_214496_) -> {
      return Component.translatable("commands.locate.poi.invalid", p_214496_);
   });
   private static final int MAX_STRUCTURE_SEARCH_RADIUS = 100;
   private static final int MAX_BIOME_SEARCH_RADIUS = 6400;
   private static final int BIOME_SAMPLE_RESOLUTION_HORIZONTAL = 32;
   private static final int BIOME_SAMPLE_RESOLUTION_VERTICAL = 64;
   private static final int POI_SEARCH_RADIUS = 256;

   public static void register(CommandDispatcher<CommandSourceStack> p_137859_) {
      p_137859_.register(Commands.literal("locate").requires((p_214470_) -> {
         return p_214470_.hasPermission(2);
      }).then(Commands.literal("structure").then(Commands.argument("structure", ResourceOrTagLocationArgument.resourceOrTag(Registry.STRUCTURE_REGISTRY)).executes((p_214507_) -> {
         return locateStructure(p_214507_.getSource(), ResourceOrTagLocationArgument.getRegistryType(p_214507_, "structure", Registry.STRUCTURE_REGISTRY, ERROR_STRUCTURE_INVALID));
      }))).then(Commands.literal("biome").then(Commands.argument("biome", ResourceOrTagLocationArgument.resourceOrTag(Registry.BIOME_REGISTRY)).executes((p_214500_) -> {
         return locateBiome(p_214500_.getSource(), ResourceOrTagLocationArgument.getRegistryType(p_214500_, "biome", Registry.BIOME_REGISTRY, ERROR_BIOME_INVALID));
      }))).then(Commands.literal("poi").then(Commands.argument("poi", ResourceOrTagLocationArgument.resourceOrTag(Registry.POINT_OF_INTEREST_TYPE_REGISTRY)).executes((p_214465_) -> {
         return locatePoi(p_214465_.getSource(), ResourceOrTagLocationArgument.getRegistryType(p_214465_, "poi", Registry.POINT_OF_INTEREST_TYPE_REGISTRY, ERROR_POI_INVALID));
      }))));
   }

   private static Optional<? extends HolderSet.ListBacked<Structure>> getHolders(ResourceOrTagLocationArgument.Result<Structure> p_214484_, Registry<Structure> p_214485_) {
      return p_214484_.unwrap().map((p_214494_) -> {
         return p_214485_.getHolder(p_214494_).map((p_214491_) -> {
            return HolderSet.direct(p_214491_);
         });
      }, p_214485_::getTag);
   }

   private static int locateStructure(CommandSourceStack p_214472_, ResourceOrTagLocationArgument.Result<Structure> p_214473_) throws CommandSyntaxException {
      Registry<Structure> registry = p_214472_.getLevel().registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY);
      HolderSet<Structure> holderset = getHolders(p_214473_, registry).orElseThrow(() -> {
         return ERROR_STRUCTURE_INVALID.create(p_214473_.asPrintable());
      });
      BlockPos blockpos = new BlockPos(p_214472_.getPosition());
      ServerLevel serverlevel = p_214472_.getLevel();
      Pair<BlockPos, Holder<Structure>> pair = serverlevel.getChunkSource().getGenerator().findNearestMapStructure(serverlevel, holderset, blockpos, 100, false);
      if (pair == null) {
         throw ERROR_STRUCTURE_NOT_FOUND.create(p_214473_.asPrintable());
      } else {
         return showLocateResult(p_214472_, p_214473_, blockpos, pair, "commands.locate.structure.success", false);
      }
   }

   private static int locateBiome(CommandSourceStack p_214502_, ResourceOrTagLocationArgument.Result<Biome> p_214503_) throws CommandSyntaxException {
      BlockPos blockpos = new BlockPos(p_214502_.getPosition());
      Pair<BlockPos, Holder<Biome>> pair = p_214502_.getLevel().findClosestBiome3d(p_214503_, blockpos, 6400, 32, 64);
      if (pair == null) {
         throw ERROR_BIOME_NOT_FOUND.create(p_214503_.asPrintable());
      } else {
         return showLocateResult(p_214502_, p_214503_, blockpos, pair, "commands.locate.biome.success", true);
      }
   }

   private static int locatePoi(CommandSourceStack p_214509_, ResourceOrTagLocationArgument.Result<PoiType> p_214510_) throws CommandSyntaxException {
      BlockPos blockpos = new BlockPos(p_214509_.getPosition());
      ServerLevel serverlevel = p_214509_.getLevel();
      Optional<Pair<Holder<PoiType>, BlockPos>> optional = serverlevel.getPoiManager().findClosestWithType(p_214510_, blockpos, 256, PoiManager.Occupancy.ANY);
      if (optional.isEmpty()) {
         throw ERROR_POI_NOT_FOUND.create(p_214510_.asPrintable());
      } else {
         return showLocateResult(p_214509_, p_214510_, blockpos, optional.get().swap(), "commands.locate.poi.success", false);
      }
   }

   public static int showLocateResult(CommandSourceStack p_214475_, ResourceOrTagLocationArgument.Result<?> p_214476_, BlockPos p_214477_, Pair<BlockPos, ? extends Holder<?>> p_214478_, String p_214479_, boolean p_214480_) {
      BlockPos blockpos = p_214478_.getFirst();
      String s = p_214476_.unwrap().map((p_214498_) -> {
         return p_214498_.location().toString();
      }, (p_214468_) -> {
         return "#" + p_214468_.location() + " (" + (String)p_214478_.getSecond().unwrapKey().map((p_214463_) -> {
            return p_214463_.location().toString();
         }).orElse("[unregistered]") + ")";
      });
      int i = p_214480_ ? Mth.floor(Mth.sqrt((float)p_214477_.distSqr(blockpos))) : Mth.floor(dist(p_214477_.getX(), p_214477_.getZ(), blockpos.getX(), blockpos.getZ()));
      String s1 = p_214480_ ? String.valueOf(blockpos.getY()) : "~";
      Component component = ComponentUtils.wrapInSquareBrackets(Component.translatable("chat.coordinates", blockpos.getX(), s1, blockpos.getZ())).withStyle((p_214489_) -> {
         return p_214489_.withColor(ChatFormatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + blockpos.getX() + " " + s1 + " " + blockpos.getZ())).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.coordinates.tooltip")));
      });
      p_214475_.sendSuccess(Component.translatable(p_214479_, s, component, i), false);
      return i;
   }

   private static float dist(int p_137854_, int p_137855_, int p_137856_, int p_137857_) {
      int i = p_137856_ - p_137854_;
      int j = p_137857_ - p_137855_;
      return Mth.sqrt((float)(i * i + j * j));
   }
}