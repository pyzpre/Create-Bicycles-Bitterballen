package net.minecraft.data;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.function.ToIntFunction;
import net.minecraft.Util;
import net.minecraft.util.GsonHelper;

public interface DataProvider {
   ToIntFunction<String> FIXED_ORDER_FIELDS = Util.make(new Object2IntOpenHashMap<>(), (p_236070_) -> {
      p_236070_.put("type", 0);
      p_236070_.put("parent", 1);
      p_236070_.defaultReturnValue(2);
   });
   Comparator<String> KEY_COMPARATOR = Comparator.comparingInt(FIXED_ORDER_FIELDS).thenComparing((p_236077_) -> {
      return p_236077_;
   });

   void run(CachedOutput p_236071_) throws IOException;

   String getName();

   static void saveStable(CachedOutput p_236073_, JsonElement p_236074_, Path p_236075_) throws IOException {
      ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
      HashingOutputStream hashingoutputstream = new HashingOutputStream(Hashing.sha1(), bytearrayoutputstream);
      Writer writer = new OutputStreamWriter(hashingoutputstream, StandardCharsets.UTF_8);
      JsonWriter jsonwriter = new JsonWriter(writer);
      jsonwriter.setSerializeNulls(false);
      jsonwriter.setIndent("  ");
      GsonHelper.writeValue(jsonwriter, p_236074_, KEY_COMPARATOR);
      jsonwriter.close();
      p_236073_.writeIfNeeded(p_236075_, bytearrayoutputstream.toByteArray(), hashingoutputstream.hash());
   }
}