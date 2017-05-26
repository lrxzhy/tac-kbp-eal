package com.bbn.kbp;

import com.bbn.bue.common.strings.offsets.CharOffset;
import com.bbn.bue.common.strings.offsets.OffsetRange;
import com.bbn.bue.common.symbols.Symbol;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public final class TacKbp2017KBLoaderTest {

  private final Set<Provenance> dummyProvenances = dummyProvenances();

  @Test
  public void testNodes() {
    final TacKbp2017KBLoader.TacKbp2017KBLoading loading =
        new TacKbp2017KBLoader.TacKbp2017KBLoading();

    final Node node1 = loading.nodeFor(":Event1");
    final Node node2 = loading.nodeFor(":Event1");
    final Node node3 = loading.nodeFor(":Event2");

    assertEquals(node1, node2);
    assertNotEquals(node1, node3);
  }

  @Test
  public void testTypeAssertion() {
    final TacKbp2017KBLoader.TacKbp2017KBLoading loading =
        new TacKbp2017KBLoader.TacKbp2017KBLoading();

    final Assertion actualAssertion = loading.parse(":String_01f\ttype\tSTRING\t0.5").assertion();
    final Assertion expectedAssertion = TypeAssertion.of(
        loading.nodeFor(":String_01f"), Symbol.from("STRING"));

    assertEquals(expectedAssertion, actualAssertion);
  }

  @Test
  public void testLinkAssertion() {
    final TacKbp2017KBLoader.TacKbp2017KBLoading loading =
        new TacKbp2017KBLoader.TacKbp2017KBLoading();

    final String line = ":Entity_aq3\tlink\t\"ExternalKB:ExternalNodeID\"   # Not a real KB ";
    final Assertion actualAssertion = loading.parse(line).assertion();

    final EntityNode subjectNode = (EntityNode) loading.nodeFor(":Entity_aq3");
    final Assertion expectedAssertion = LinkAssertion.of(
        subjectNode, Symbol.from("ExternalKB"), Symbol.from("ExternalNodeID"));

    assertEquals(expectedAssertion, actualAssertion);
  }

  @Test
  public void testSentimentAssertion() {
    final TacKbp2017KBLoader.TacKbp2017KBLoading loading =
        new TacKbp2017KBLoader.TacKbp2017KBLoading();

    final String line = ":Entity1\tper:dislikes\t:Entity2\tdocID:5-12,docID:5-12;10-15\t\t";
    final Assertion actualAssertion = loading.parse(line).assertion();
    final Assertion expectedAssertion = SentimentAssertion.builder()
        .subject((EntityNode) loading.nodeFor(":Entity1"))
        .object((EntityNode) loading.nodeFor(":Entity2"))
        .subjectEntityType(Symbol.from("per"))
        .sentiment(Symbol.from("dislikes"))
        .provenances(dummyProvenances)
        .build();

    assertEquals(expectedAssertion, actualAssertion);
  }

  @Test
  public void testSFAssertion() {
    final TacKbp2017KBLoader.TacKbp2017KBLoading loading =
        new TacKbp2017KBLoader.TacKbp2017KBLoading();

    final String line = ":Entity1\tper:age\t:String1\tdocID:5-12,docID:5-12;10-15\t#t\t";
    final Assertion actualAssertion = loading.parse(line).assertion();
    final Assertion expectedAssertion = SFAssertion.builder()
        .subject((EntityNode) loading.nodeFor(":Entity1"))
        .object((StringNode) loading.nodeFor(":String1"))
        .subjectEntityType(Symbol.from("per"))
        .relation(Symbol.from("age"))
        .provenances(dummyProvenances)
        .build();

    assertEquals(expectedAssertion, actualAssertion);
  }

  @Test
  public void testEventArgumentAssertion() {
    final TacKbp2017KBLoader.TacKbp2017KBLoading loading =
        new TacKbp2017KBLoader.TacKbp2017KBLoading();

    final String line = ":Event_0\tlife.die:victim.actual\t:Entity_0\tdocID:5-12,docID:5-12;10-15";
    final Assertion actualAssertion = loading.parse(line).assertion();
    final Assertion expectedAssertion = EventArgumentAssertion.builder()
        .subject((EventNode) loading.nodeFor(":Event_0"))
        .argument((EntityNode) loading.nodeFor(":Entity_0"))
        .eventType(Symbol.from("life.die"))
        .role(Symbol.from("victim"))
        .realis(Symbol.from("actual"))
        .provenances(dummyProvenances)
        .build();

    assertEquals(expectedAssertion, actualAssertion);
  }

  @Test
  public void testInverseEventArgumentAssertion() {
    final TacKbp2017KBLoader.TacKbp2017KBLoading loading =
        new TacKbp2017KBLoader.TacKbp2017KBLoading();

    final String line =
        ":Entity_0\tper:life.die_victim.actual\t:Event_0\tdocID:5-12,docID:5-12;10-15";
    final Assertion actualAssertion = loading.parse(line).assertion();
    final Assertion expectedAssertion = EventArgumentAssertion.builder()
        .subject((EventNode) loading.nodeFor(":Event_0"))
        .argument((EntityNode) loading.nodeFor(":Entity_0"))
        .eventType(Symbol.from("life.die"))
        .role(Symbol.from("victim"))
        .realis(Symbol.from("actual"))
        .provenances(dummyProvenances)
        .build();

    assertEquals(expectedAssertion, actualAssertion);
  }

  @Test
  public void testMentionAssertion() {
    final TacKbp2017KBLoader.TacKbp2017KBLoading loading =
        new TacKbp2017KBLoader.TacKbp2017KBLoading();

    final String line =
        ":Event_0\tcanonical_mention.actual\t\"dummy\\\"mention\\\"\"\tdocID:5-12,docID:5-12;10-15";
    final Assertion actualAssertion = loading.parse(line).assertion();
    final Assertion expectedAssertion = EventCanonicalMentionAssertion.of(
        (EventNode) loading.nodeFor(":Event_0"),
        "dummy\"mention\"",
        Symbol.from("actual"),
        dummyProvenances);

    assertEquals(expectedAssertion, actualAssertion);
  }

  @Test
  public void testConfidence() {
    final TacKbp2017KBLoader.TacKbp2017KBLoading loading =
        new TacKbp2017KBLoader.TacKbp2017KBLoading();

    final double confidence = loading.parse(":String_01f\ttype\tSTRING\t0.5  ").confidence().get();
    assertEquals(0.5, confidence, 0.0);
  }

  @Test
  public void testLoad() throws IOException {
    final KnowledgeBaseLoader loader = TacKbp2017KBLoader.create();

    final String inputString = "dummy_runID\n"
        + "\n# This is a dummy comment. \n \n"
        + ":Event_0\ttype\tCONFLICT.ATTACK\t0.900  \n"
        + ":Event_0\tmention.actual\t\"dummy\\\"mention\\\"\"\tdocID:5-12,docID:5-12;10-15";

    final File inputFile = File.createTempFile("kb-loader-test", ".tmp");
    Files.write(inputString, inputFile, Charsets.UTF_8);
    final KnowledgeBase actualKB = loader.load(Files.asCharSource(inputFile, Charsets.UTF_8));
    inputFile.deleteOnExit();

    final EventNode node = (EventNode) actualKB.nodes().asList().get(0);
    final Assertion assertion1 = TypeAssertion.of(node, Symbol.from("CONFLICT.ATTACK"));
    final Assertion assertion2 = EventMentionAssertion.of(
        node, "dummy\"mention\"", Symbol.from("actual"), dummyProvenances);

    final KnowledgeBase expectedKB = KnowledgeBase.builder()
        .runId(Symbol.from("dummy_runID"))
        .addNodes(node)
        .addAssertions(assertion1, assertion2)
        .putConfidence(assertion1, 0.9)
        .build();

    assertEquals(expectedKB, actualKB);
  }

  private Set<Provenance> dummyProvenances() {
    final Symbol docId = Symbol.from("docID");
    final OffsetRange<CharOffset> offset1 = OffsetRange.charOffsetRange(5, 12);
    final OffsetRange<CharOffset> offset2 = OffsetRange.charOffsetRange(10, 15);
    final Provenance provenance1 = Provenance.of(docId, ImmutableSet.of(offset1));
    final Provenance provenance2 = Provenance.of(docId, ImmutableSet.of(offset1, offset2));

    return ImmutableSet.of(provenance1, provenance2);
  }

}