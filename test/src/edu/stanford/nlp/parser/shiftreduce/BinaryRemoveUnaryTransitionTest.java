package edu.stanford.nlp.parser.shiftreduce;

import junit.framework.TestCase;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.ling.TaggedWord;

import java.util.Arrays;
import java.util.List;

/**
 * Test a couple transition operations and their effects
 * <br>
 * This transition type should remove unwanted unary transitions
 *
 * @author John Bauer
 */
public class BinaryRemoveUnaryTransitionTest extends TestCase {
  // TODO: add tests for isLegal
  // test states where BinaryTransition could not apply (eg stack too small)
  // test compound transitions

  public static State buildState(int shifts, boolean compound) {
    String[] words = { "This", "is", "a", "short", "test", "." };
    String[] tags = { "DT", "VBZ", "DT", "JJ", "NN", "." };
    assertEquals(words.length, tags.length);
    List<TaggedWord> sentence = SentenceUtils.toTaggedList(Arrays.asList(words), Arrays.asList(tags));
    State state = ShiftReduceParser.initialStateFromTaggedSentence(sentence);

    ShiftTransition shift = new ShiftTransition();
    for (int i = 0; i < shifts - 1; ++i) {
      state = shift.apply(state);
    }
    String[] unaries = {"VP", "NP"};
    if (compound) {
      List<String> transitions = Arrays.asList(unaries);
      CompoundUnaryTransition unary = new CompoundUnaryTransition(transitions, false);
      state = unary.apply(state);
    } else {
      for (String tran : unaries) {
        UnaryTransition unary = new UnaryTransition(tran, false);
        state = unary.apply(state);
      }
    }
    state = shift.apply(state);
    assertEquals(shifts, state.tokenPosition);
    return state;
  }

  public void testNeedsUnary() {
    State state = BinaryTransitionTest.buildState(2);
    BinaryRemoveUnaryTransition transition = new BinaryRemoveUnaryTransition("NP", BinaryTransition.Side.LEFT, false);
    // should be illegal if there are no Unary transitions here
    assertFalse(transition.isLegal(state, null));    
  }
  
  public void testCompoundUnaryTransition() {
    State state = buildState(2, true);
    BinaryRemoveUnaryTransition transition = new BinaryRemoveUnaryTransition("NP", BinaryTransition.Side.LEFT, false);
    assertTrue(transition.isLegal(state, null));    
    state = transition.apply(state);
    assertFalse(transition.isLegal(state, null));    

    assertEquals(2, state.tokenPosition);
    assertEquals(1, state.stack.size());
    assertEquals(2, state.stack.peek().children().length);
    assertEquals("NP", state.stack.peek().value());
    BinaryTransitionTest.checkHeads(state.stack.peek(), state.stack.peek().children()[0]);
    Tree tree = state.stack.peek();
    assertEquals(2, tree.children().length);
    assertTrue(tree.children()[0].isPreTerminal());
    assertTrue(tree.children()[1].isPreTerminal());
  }
  
  public void testUnaryTransition() {
    State state = buildState(2, false);
    BinaryRemoveUnaryTransition transition = new BinaryRemoveUnaryTransition("NP", BinaryTransition.Side.LEFT, false);
    assertTrue(transition.isLegal(state, null));    
    state = transition.apply(state);
    assertFalse(transition.isLegal(state, null));    

    assertEquals(2, state.tokenPosition);
    assertEquals(1, state.stack.size());
    assertEquals(2, state.stack.peek().children().length);
    assertEquals("NP", state.stack.peek().value());
    BinaryTransitionTest.checkHeads(state.stack.peek(), state.stack.peek().children()[0]);
    Tree tree = state.stack.peek();
    assertEquals(2, tree.children().length);
    assertTrue(tree.children()[0].isPreTerminal());
    assertTrue(tree.children()[1].isPreTerminal());
  }
}
