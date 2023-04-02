package io.tashtabash.generator.culture.worldview.reasoning.labeler

import io.tashtabash.generator.culture.worldview.reasoning.concept.ReasonConcept
import io.tashtabash.utils.labeler.ConcatLabeler
import io.tashtabash.utils.labeler.DisjunctionLabeler
import io.tashtabash.utils.labeler.EqualityLabeler
import io.tashtabash.utils.labeler.Labeler


typealias ConceptLabeler = Labeler<ReasonConcept>
typealias EqualityConceptLabeler = EqualityLabeler<ReasonConcept>
typealias ConcatConceptLabeler = ConcatLabeler<ReasonConcept>
typealias DisjunctionConceptLabeler = DisjunctionLabeler<ReasonConcept>
