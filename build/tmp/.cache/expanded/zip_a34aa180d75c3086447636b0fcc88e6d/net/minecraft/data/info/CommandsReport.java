package net.minecraft.data.info;

import com.mojang.brigadier.CommandDispatcher;
import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;

public class CommandsReport implements DataProvider {
   private final DataGenerator generator;

   public CommandsReport(DataGenerator p_124045_) {
      this.generator = p_124045_;
   }

   public void run(CachedOutput p_236199_) throws IOException {
      Path path = this.generator.getOutputFolder(DataGenerator.Target.REPORTS).resolve("commands.json");
      CommandDispatcher<CommandSourceStack> commanddispatcher = (new Commands(Commands.CommandSelection.ALL, new CommandBuildContext(RegistryAccess.BUILTIN.get()))).getDispatcher();
      DataProvider.saveStable(p_236199_, ArgumentUtils.serializeNodeToJson(commanddispatcher, commanddispatcher.getRoot()), path);
   }

   public String getName() {
      return "Command Syntax";
   }
}