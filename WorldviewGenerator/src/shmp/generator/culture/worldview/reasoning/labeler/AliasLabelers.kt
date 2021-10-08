package shmp.generator.culture.worldview.reasoning.labeler

import shmp.generator.culture.worldview.reasoning.concept.ReasonConcept
import shmp.utils.labeler.ConcatLabeler
import shmp.utils.labeler.DisjunctionLabeler
import shmp.utils.labeler.EqualityLabeler
import shmp.utils.labeler.Labeler


typealias ConceptLabeler = Labeler<ReasonConcept>
typealias EqualityConceptLabeler = EqualityLabeler<ReasonConcept>
typealias ConcatConceptLabeler = ConcatLabeler<ReasonConcept>
typealias DisjunctionConceptLabeler = DisjunctionLabeler<ReasonConcept>
