package lisa.utilities
import lisa.test.ProofCheckerSuite
import lisa.utils.Printer

import lisa.proven.tactics.Destructors.*
import lisa.proven.tactics.ProofTactics.*
import lisa.utils.Helpers.given_Conversion_VariableLabel_VariableTerm

import lisa.kernel.proof.SCProof
import org.scalatest.funsuite.AnyFunSuite
import scala.language.adhocExtensions
import lisa.kernel.fol.*
import scala.collection.immutable.NumericRange
class Transformations extends ProofCheckerSuite {
    import lisa.proven.SetTheoryLibrary.*

    
    test("Trasnsformation initialises well with empty proof and returns an empty proof") {
        val nullSCProof = SCProof()
        val transf = lisa.utilities.prooftransform.ProofUnconditionalizer(nullSCProof)
        assert(transf.transform() == nullSCProof)
        
    }

    /**
     * Any proof where there are no imports shoud not be modified 
     * Dummy proofs of varying size should be tested
     **/
    test("A proof with no imports is not modified") {
        val phi = SchematicNPredicateLabel("phi", 0)

        val intro = Hypothesis((phi()) |- (phi()), phi())
        val outro = Rewrite((phi()) |- (phi()), 0)
        
        val noImpProof = SCProof(IndexedSeq(intro, outro), IndexedSeq.empty)
        val transf = lisa.utilities.prooftransform.ProofUnconditionalizer(noImpProof)
        assert((transf.transform() == noImpProof))
        checkProof(noImpProof)
    }

   test("A proof with imports is to be modified") {
        val phi = SchematicNPredicateLabel("phi", 0)

        val intro = Rewrite(() |- phi(), -1)
        val outro = Weakening(intro.bot.right|- intro.bot.right, 0)
        
        val noImpProof = SCProof(IndexedSeq(intro, outro), IndexedSeq(intro.bot))
        val transf = lisa.utilities.prooftransform.ProofUnconditionalizer(noImpProof).transform()
        
        checkProof(transf)
        assert(transf != noImpProof )
        assert(isSameSequent(transf.steps.head.bot, (sequentToFormula(intro.bot)) |- intro.bot.right))
    }

    test("A proof with imports and a step taking multiple premises should be modified accordingly") {
        val phi = SchematicNPredicateLabel("phi", 0)()
        val psi = SchematicNPredicateLabel("psi", 0)()

        val into1 = Rewrite(() |- phi, -2)
        val into2 = Rewrite(() |- psi, -1)
        val merge = RightAnd(() |- ConnectorFormula(And,(into1.bot.right ++ into2.bot.right).toSeq), Seq(-2,0), (into1.bot.right ++ into2.bot.right).toSeq)

        val noImpProof = SCProof(IndexedSeq(into2, merge), IndexedSeq(into2.bot, into1.bot))
        val transf = lisa.utilities.prooftransform.ProofUnconditionalizer(noImpProof).transform()
        checkProof(transf)
        assert(transf != noImpProof )
        assert(isSameSequent(transf.steps.head.bot, (sequentToFormula(into1.bot)) |- into1.bot.right))
    }

    test("A proof with imports and a subproof should be modified accordingly") {
        val phi = SchematicNPredicateLabel("phi", 0)()
        val intro = Rewrite(() |- phi, -1)
        val outro = SCSubproof(SCProof(IndexedSeq(Weakening(intro.bot.right|- intro.bot.right, -1)), IndexedSeq(intro.bot)), IndexedSeq(0))
        val noImpProof = SCProof(IndexedSeq(intro, outro), IndexedSeq(intro.bot))
        val transf = lisa.utilities.prooftransform.ProofUnconditionalizer(noImpProof).transform()
        
        checkProof(transf)
        assert(transf != noImpProof )
        assert(isSameSequent(transf.steps.head.bot, (sequentToFormula(intro.bot)) |- intro.bot.right))

    }

    test("A proof with imports and a complete instantiation should be modified accordingly") {
        val phi = SchematicNPredicateLabel("phi", 0)
        val psi = SchematicNPredicateLabel("psi", 2)
        val x = VariableLabel("x")
        val y = VariableLabel("y")

        val intro = Rewrite(() |- phi(), -1)
        val outro = InstPredSchema(() |- psi(x, y), 0,  Map((phi , LambdaTermFormula(Seq(), psi(x, y)))))
        val noImpProof = SCProof(IndexedSeq(intro, outro), IndexedSeq(intro.bot))
        val transf = lisa.utilities.prooftransform.ProofUnconditionalizer(noImpProof).transform()
        checkProof(transf)
        assert(transf != noImpProof )
        assert(isSameSequent(transf.steps.head.bot, (sequentToFormula(intro.bot)) |- intro.bot.right))


    }

    test("A proof with imports and two partial instantiations should be modified accordingly") {
        val phi = SchematicNPredicateLabel("phi", 0)
        val psi = SchematicNPredicateLabel("psi", 2)
        val x = VariableLabel("x")
        val y = VariableLabel("y")

        val intro = Rewrite(() |- phi(), -1)
        val mid = InstPredSchema(() |- psi(x, y) <=> phi(), 0,  Map((phi , LambdaTermFormula(Seq(), psi(x, y) <=> phi()))))
        val outro = InstPredSchema(() |- psi(x, y) <=> psi(x, y), 1,  Map((phi , LambdaTermFormula(Seq(), psi(x, y)))))
        val noImpProof = SCProof(IndexedSeq(intro, mid, outro), IndexedSeq(intro.bot))
        val transf = lisa.utilities.prooftransform.ProofUnconditionalizer(noImpProof).transform()
        info(Printer.prettySCProof(noImpProof))
        checkProof(transf)
        assert(transf != noImpProof )
        assert(isSameSequent(transf.steps.head.bot, (sequentToFormula(intro.bot)) |- intro.bot.right))
    }
}
