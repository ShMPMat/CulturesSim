package shmp.simulation.culture.group.cultureaspect.reasoning.concept

import shmp.simulation.culture.thinking.meaning.Meme
import shmp.simulation.culture.thinking.meaning.MemeSubject


sealed class IdeationalConcept(
        override val meme: Meme,
        _oppositeConcepts: () -> List<ReasonConcept>,
        _correspondingConcepts: () -> List<ReasonConcept>
) : AbstractKotlinBugSafeReasonConcept(_oppositeConcepts, _correspondingConcepts) {
    object Good : IdeationalConcept(MemeSubject("Good"), { listOf(Bad) }, { listOf() })
    object Bad : IdeationalConcept(MemeSubject("Bad"), { listOf(Good) }, { listOf() })
    object NoEvaluation : IdeationalConcept(MemeSubject("NoEvaluation"), { listOf(Good, Bad) }, { listOf() })
    object Uncertainty : IdeationalConcept(MemeSubject("Uncertainty"), { listOf() }, { listOf() })

    object Peace : IdeationalConcept(MemeSubject("Peace"), { listOf(War, Death) }, { listOf() })
    object War : IdeationalConcept(MemeSubject("War"), { listOf(Peace) }, { listOf() })

    object Expansion : IdeationalConcept(MemeSubject("Expansion"), { listOf(Content) }, { listOf() })
    object Content : IdeationalConcept(MemeSubject("Content"), { listOf(Expansion) }, { listOf() })

    object Consolidation : IdeationalConcept(MemeSubject("Consolidation"), { listOf(Freedom) }, { listOf() })
    object Freedom : IdeationalConcept(MemeSubject("Freedom"), { listOf(Consolidation) }, { listOf() })

    object Creation : IdeationalConcept(MemeSubject("Creation"), { listOf(Destruction) }, { listOf() })
    object Destruction : IdeationalConcept(MemeSubject("Destruction"), { listOf(Creation) }, { listOf() })

    object Hardship : IdeationalConcept(MemeSubject("Hardship"), { listOf(Comfort) }, { listOf() })
    object Comfort : IdeationalConcept(MemeSubject("Comfort"), { listOf(Hardship) }, { listOf() })

    object Simpleness : IdeationalConcept(MemeSubject("Simpleness"), { listOf(Hardness) }, { listOf() })
    object Hardness : IdeationalConcept(MemeSubject("Hardness"), { listOf(Simpleness) }, { listOf() })

    object Importance : IdeationalConcept(MemeSubject("Importance"), { listOf(Unimportance) }, { listOf() })
    object Unimportance : IdeationalConcept(MemeSubject("Unimportance"), { listOf(Importance) }, { listOf() })

    object Change : IdeationalConcept(MemeSubject("Change"), { listOf(Permanence) }, { listOf() })
    object Permanence : IdeationalConcept(MemeSubject("Permanence"), { listOf(Change) }, { listOf() })

    object Improvement : IdeationalConcept(MemeSubject("Improvement"), { listOf(Degradation) }, { listOf() })
    object Degradation : IdeationalConcept(MemeSubject("Degradation"), { listOf(Improvement) }, { listOf() })

    object Life : IdeationalConcept(MemeSubject("Life"), { listOf(Death) }, { listOf() })
    object Death : IdeationalConcept(MemeSubject("Death"), { listOf(Life) }, { listOf() })

    object Simplicity : IdeationalConcept(MemeSubject("Simplicity"), { listOf(Complexity) }, { listOf() })
    object Complexity : IdeationalConcept(MemeSubject("Complexity"), { listOf(Simplicity) }, { listOf() })

    object Defence : IdeationalConcept(MemeSubject("Defence"), { listOf(Abandonment) }, { listOf() })
    object Abandonment : IdeationalConcept(MemeSubject("Negligence"), { listOf(Defence) }, { listOf() })

    object Commonness : IdeationalConcept(MemeSubject("Commonness"), { listOf(Uniqueness, Rareness) }, { listOf() })
    object Uniqueness : IdeationalConcept(MemeSubject("Uniqueness"), { listOf(Commonness) }, { listOf() })
    object Rareness : IdeationalConcept(MemeSubject("Rareness"), { listOf(Commonness) }, { listOf() })

    object Beauty : IdeationalConcept(MemeSubject("Beauty"), { listOf(Ugliness) }, { listOf() })
    object Ugliness : IdeationalConcept(MemeSubject("Ugliness"), { listOf(Beauty) }, { listOf() })

    object Work : IdeationalConcept(MemeSubject("Work"), { listOf(Rest) }, { listOf() })
    object Rest : IdeationalConcept(MemeSubject("Rest"), { listOf(Work) }, { listOf() })
}