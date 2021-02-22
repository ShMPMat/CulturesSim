package shmp.simulation.culture.group.cultureaspect.reasoning.labeler

import shmp.simulation.culture.group.cultureaspect.concept.ReasonConcept
import shmp.utils.labeler.ConcatLabeler
import shmp.utils.labeler.DisjunctionLabeler
import shmp.utils.labeler.EqualityLabeler
import shmp.utils.labeler.Labeler


typealias ConceptLabeler = Labeler<ReasonConcept>
typealias EqualityConceptLabeler = EqualityLabeler<ReasonConcept>
typealias ConcatConceptLabeler = ConcatLabeler<ReasonConcept>
typealias DisjunctionConceptLabeler = DisjunctionLabeler<ReasonConcept>
