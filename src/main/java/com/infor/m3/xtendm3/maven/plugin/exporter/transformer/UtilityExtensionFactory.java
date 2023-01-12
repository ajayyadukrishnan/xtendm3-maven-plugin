package com.infor.m3.xtendm3.maven.plugin.exporter.transformer;

import com.infor.m3.xtendm3.maven.plugin.model.entity.BaseExtensionMetadata;
import com.infor.m3.xtendm3.maven.plugin.model.entity.UtilityExtensionMetadata;
import com.infor.m3.xtendm3.maven.plugin.model.internal.Extension;
import com.infor.m3.xtendm3.maven.plugin.model.internal.ProgramModule;
import com.infor.m3.xtendm3.maven.plugin.model.internal.Source;
import com.infor.m3.xtendm3.maven.plugin.model.internal.Utility;
import com.infor.m3.xtendm3.maven.plugin.model.type.APIVersion;
import com.infor.m3.xtendm3.maven.plugin.model.type.ErrorCode;
import com.infor.m3.xtendm3.maven.plugin.util.AssertionUtils;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

class UtilityExtensionFactory implements ExtensionFactory {

  @Override
  public Extension create(BaseExtensionMetadata extensionMetadata, File extension) throws MojoFailureException {
    UtilityExtensionMetadata metadata = (UtilityExtensionMetadata) extensionMetadata;
    Source source = buildSource(extension);
    Utility utility = buildUtility(metadata, source);
    ProgramModule programModule = buildProgramModule(utility);
    return buildExtension(Collections.singletonList(programModule), Collections.singletonList(source));
  }

  private Source buildSource(File extension) throws MojoFailureException {
    try {
      return Source.builder()
        .apiVersion(APIVersion.UNKNOWN)
        .beVersion(DEFAULT_BE_VERSION)
        .created(Instant.now().toEpochMilli())
        .updated(Instant.now().toEpochMilli())
        .createdBy(DEFAULT_CREATOR)
        .updatedBy(DEFAULT_UPDATER)
        .uuid(UUID.randomUUID().toString())
        .code(Files.readAllBytes(extension.toPath()))
        .build();
    } catch (IOException e) {
      AssertionUtils.getInstance().fail(ErrorCode.FILE_EXTENSION_LOOKUP_ERROR, extension.getName());
    }
    throw new MojoFailureException("Code will not reach here");
  }

  private Utility buildUtility(UtilityExtensionMetadata metadata, Source source) {
    return Utility.builder()
      .name(metadata.getName())
      .sourceUuid(source.getUuid())
      .build();
  }

  private ProgramModule buildProgramModule(Utility utility) {
    return ProgramModule.builder()
      .program("")
      .transactions(Collections.emptyMap())
      .triggers(Collections.emptyMap())
      .batches(Collections.emptyMap())
      .utilities(Collections.singletonMap(utility.getName(), utility))
      .build();
  }

  private Extension buildExtension(List<ProgramModule> programModules, List<Source> sources) {
    return Extension.builder()
      .programModules(programModules.stream().collect(Collectors.toMap(ProgramModule::getProgram, module -> module)))
      .sources(sources.stream().collect(Collectors.toMap(Source::getUuid, source -> source)))
      .build();
  }
}
