package org.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.robolectric.res.builder.XmlBlock;

import java.io.IOException;
import java.io.InputStream;

// TODO: Give me a better name
abstract class XResourceLoader extends ResourceLoader {
  final ResBunch data = new ResBunch();
  final ResBundle xmlDocuments = new ResBundle();
  final ResBundle rawResources = new ResBundle();
  
  private final ResourceIndex resourceIndex;
  private boolean isInitialized = false;

  XResourceLoader(ResourceIndex resourceIndex) {
    this.resourceIndex = resourceIndex;
  }

  abstract void doInitialize();

  synchronized void initialize() {
    if (isInitialized) return;
    doInitialize();
    isInitialized = true;

    makeImmutable();
  }

  private void makeImmutable() {
    data.makeImmutable();

    xmlDocuments.makeImmutable();
    rawResources.makeImmutable();
  }

  public TypedResource getValue(@NotNull ResName resName, String qualifiers) {
    initialize();
    ResBundle.Value<TypedResource> value = data.getValue(resName, qualifiers);
    return value == null ? null : value.getValue();
  }

  @Override
  public XmlBlock getXml(ResName resName, String qualifiers) {
    initialize();
    return (XmlBlock) xmlDocuments.get(resName, qualifiers).getData();
  }

  @Override
  public InputStream getRawValue(ResName resName) {
    initialize();

    FsFile file = (FsFile) rawResources.get(resName, "").getData();
    try {
      return file == null ? null : file.getInputStream();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public ResourceIndex getResourceIndex() {
    return resourceIndex;
  }

  @Override
  public void receive(Visitor visitor) {
    initialize();
    data.receive(visitor);
  }
}
