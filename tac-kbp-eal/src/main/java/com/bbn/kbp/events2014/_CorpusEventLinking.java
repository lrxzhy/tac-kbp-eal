package com.bbn.kbp.events2014;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;

import org.immutables.func.Functional;
import org.immutables.value.Value;

import static com.bbn.bue.common.collections.IterableUtils.noneEqualForHashable;
import static com.bbn.kbp.events2014.CorpusEventFrameFunctions.id;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * The collection of all cross-document events found in a corpus.
 */
// old code, we don't care if it uses deprecated stuff
@SuppressWarnings("deprecation")
@Value.Immutable
@com.bbn.bue.common.TextGroupPublicImmutable
@Functional
abstract class _CorpusEventLinking {

  @Value.Parameter
  public abstract ImmutableSet<CorpusEventFrame> corpusEventFrames();

  @Value.Derived
  public ImmutableMultimap<DocEventFrameReference, CorpusEventFrame> docEventsToCorpusEvents() {
    final ImmutableMultimap.Builder<DocEventFrameReference, CorpusEventFrame> ret =
        ImmutableMultimap.builder();

    for (final CorpusEventFrame corpusEventFrame : corpusEventFrames()) {
      for (final DocEventFrameReference docEventFrameReference : corpusEventFrame
          .docEventFrames()) {
        ret.put(docEventFrameReference, corpusEventFrame);
      }
    }

    return ret.build();
  }

  @Value.Check
  protected void check() {
    checkArgument(noneEqualForHashable(FluentIterable.from(corpusEventFrames())
        .transform(id())));
  }
}
