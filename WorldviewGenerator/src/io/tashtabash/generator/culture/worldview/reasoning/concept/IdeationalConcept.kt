package io.tashtabash.generator.culture.worldview.reasoning.concept

import io.tashtabash.generator.culture.worldview.Meme


sealed class IdeationalConcept(
        override val meme: Meme,
        _oppositeConcepts: () -> List<ReasonConcept>,
        _correspondingConcepts: () -> List<ReasonConcept>
) : AbstractKotlinSafeReasonConcept(_oppositeConcepts, _correspondingConcepts) {
    object Good :          IdeationalConcept(Meme("Good"),          { listOf(Bad) },                  { listOf() })
    object Bad :           IdeationalConcept(Meme("Bad"),           { listOf(Good) },                 { listOf() })
    object NoEvaluation :  IdeationalConcept(Meme("NoEvaluation"),  { listOf(Good, Bad) },            { listOf() })
    object Uncertainty :   IdeationalConcept(Meme("Uncertainty"),   { listOf() },                     { listOf() })

    object Peace :         IdeationalConcept(Meme("Peace"),         { listOf(War, Death) },           { listOf() })
    object War :           IdeationalConcept(Meme("War"),           { listOf(Peace) },                { listOf() })

    object Expansion :     IdeationalConcept(Meme("Expansion"),     { listOf(Content) },              { listOf() })
    object Content :       IdeationalConcept(Meme("Content"),       { listOf(Expansion) },            { listOf() })

    object Consolidation : IdeationalConcept(Meme("Consolidation"), { listOf(Freedom) },              { listOf() })
    object Freedom :       IdeationalConcept(Meme("Freedom"),       { listOf(Consolidation) },        { listOf() })

    object Creation :      IdeationalConcept(Meme("Creation"),      { listOf(Destruction) },          { listOf() })
    object Destruction :   IdeationalConcept(Meme("Destruction"),   { listOf(Creation) },             { listOf() })

    object Hardship :      IdeationalConcept(Meme("Hardship"),      { listOf(Comfort) },              { listOf() })
    object Comfort :       IdeationalConcept(Meme("Comfort"),       { listOf(Hardship) },             { listOf() })

    object Simpleness :    IdeationalConcept(Meme("Simpleness"),    { listOf(Hardness) },             { listOf() })
    object Hardness :      IdeationalConcept(Meme("Hardness"),      { listOf(Simpleness) },           { listOf() })

    object Importance :    IdeationalConcept(Meme("Importance"),    { listOf(Unimportance) },         { listOf() })
    object Unimportance :  IdeationalConcept(Meme("Unimportance"),  { listOf(Importance) },           { listOf() })

    object Change :        IdeationalConcept(Meme("Change"),        { listOf(Permanence) },           { listOf() })
    object Permanence :    IdeationalConcept(Meme("Permanence"),    { listOf(Change) },               { listOf() })

    object Improvement :   IdeationalConcept(Meme("Improvement"),   { listOf(Degradation) },          { listOf() })
    object Degradation :   IdeationalConcept(Meme("Degradation"),   { listOf(Improvement) },          { listOf() })

    object Life :          IdeationalConcept(Meme("Life"),          { listOf(Death) },                { listOf() })
    object Death :         IdeationalConcept(Meme("Death"),         { listOf(Life) },                 { listOf() })

    object Simplicity :    IdeationalConcept(Meme("Simplicity"),    { listOf(Complexity) },           { listOf() })
    object Complexity :    IdeationalConcept(Meme("Complexity"),    { listOf(Simplicity) },           { listOf() })

    object Defence :       IdeationalConcept(Meme("Defence"),       { listOf(Abandonment) },          { listOf() })
    object Abandonment :   IdeationalConcept(Meme("Negligence"),    { listOf(Defence) },              { listOf() })

    object Commonness :    IdeationalConcept(Meme("Commonness"),    { listOf(Uniqueness, Rareness) }, { listOf() })
    object Uniqueness :    IdeationalConcept(Meme("Uniqueness"),    { listOf(Commonness) },           { listOf(Rareness) })
    object Rareness :      IdeationalConcept(Meme("Rareness"),      { listOf(Commonness) },           { listOf(Uniqueness) })

    object Beauty :        IdeationalConcept(Meme("Beauty"),        { listOf(Ugliness) },             { listOf() })
    object Ugliness :      IdeationalConcept(Meme("Ugliness"),      { listOf(Beauty) },               { listOf() })

    object Work :          IdeationalConcept(Meme("Work"),          { listOf(Rest) },                 { listOf() })
    object Rest :          IdeationalConcept(Meme("Rest"),          { listOf(Work) },                 { listOf() })

    object Fear :          IdeationalConcept(Meme("Fear"),          { listOf(Courage) },              { listOf() })
    object Courage :       IdeationalConcept(Meme("Courage"),       { listOf(Fear) },                 { listOf() })

    object Safety :        IdeationalConcept(Meme("Safety"),        { listOf(Danger, Hardship) },     { listOf(Comfort) })
    object Danger :        IdeationalConcept(Meme("Danger"),        { listOf(Safety, Comfort) },      { listOf(Hardship) })

    object Spirituality :  IdeationalConcept(Meme("Spirituality"),  { listOf() },                     { listOf() })

    object Luck :          IdeationalConcept(Meme("Luck"),          { listOf(Misfortune) },           { listOf() })
    object Misfortune :    IdeationalConcept(Meme("Misfortune"),    { listOf(Luck) },                 { listOf() })

    object Mortality :     IdeationalConcept(Meme("Mortality"),     { listOf(Immortality) },          { listOf() })
    object Immortality :   IdeationalConcept(Meme("Immortality"),   { listOf(Mortality) },            { listOf() })
}

val ideationalConcepts = IdeationalConcept::class.sealedSubclasses
        .mapNotNull { it.objectInstance }
