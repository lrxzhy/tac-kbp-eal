package com.bbn.kbp.events2014.transformers;

import com.bbn.kbp.events2014.ScoringData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by rgabbard on 6/25/15.
 */
public class ApplyAnswerKeyToResponseMappingRuleToAll implements ScoringDataTransformation {
  private static final Logger log = LoggerFactory
      .getLogger(ApplyAnswerKeyToResponseMappingRuleToAll.class);

  private final AnswerKeyToResponseMappingRule rule;

  private ApplyAnswerKeyToResponseMappingRuleToAll(
      final AnswerKeyToResponseMappingRule rule) {
    this.rule = checkNotNull(rule);
  }

  public static ScoringDataTransformation forRuleApplyingToBoth(AnswerKeyToResponseMappingRule rule) {
    return new ApplyAnswerKeyToResponseMappingRuleToAll(rule);
  }

  @Override
  public ScoringData transform(ScoringData input) {
    checkArgument(input.answerKey().isPresent(), "Cannot apply a transformation based on "
        + "the answer key if it is absent");
    input.answerKey().get().checkCompletelyAssesses(input.argumentOutput().get());
    final ResponseMapping responseMapping = rule.computeResponseTransformation(
        input.answerKey().get());
    if (!responseMapping.isIdentity()) {
      log.info("Rule {} resulted in {}", rule, responseMapping.summaryString());

      ScoringData.Builder ret = ScoringData.builder().from(input)
          .answerKey(responseMapping.apply(input.answerKey().get()));
      if (input.argumentOutput().isPresent()) {
        ret.argumentOutput(responseMapping.apply(input.argumentOutput().get()));
      }
      if (input.systemLinking().isPresent()) {
        ret.systemLinking(responseMapping.apply(input.systemLinking().get()));
      }
      if (input.referenceLinking().isPresent()) {
        ret.referenceLinking(responseMapping.apply(input.referenceLinking().get()));
      }

      return ret.build();
    } else {
      return input;
    }
  }

  @Override
  public void logStats() {

  }
}
