package de.pixelgerecht.riekespaarefinden.memory

import org.assertj.core.api.Assertions
import org.junit.Test
import java.lang.IllegalArgumentException
import java.util.*

internal class DeckTest
{
    @Test
    fun motiveSetMustBeNonEmpty() {
        Assertions
            .assertThatExceptionOfType(IllegalArgumentException::class.java)
            .isThrownBy { Deck(Collections.emptySet()) }
    }

    @Test
    fun returnsSizeOfMotiveSet() {
        Assertions
            .assertThat(Deck(setOf(Motive("foo"))).motiveCount())
            .isEqualTo(1)

        Assertions
            .assertThat(Deck(setOf(Motive("foo"), Motive("bar"))).motiveCount())
            .isEqualTo(2)
    }

    @Test
    fun returnsEqualMotiveSet() {
        val set1 = setOf(Motive("foo"))

        Assertions
            .assertThat(Deck(set1).motiveSet())
            .isEqualTo(set1)


        val set2 = setOf(Motive("foo"), Motive("bar"))

        Assertions
            .assertThat(Deck(set2).motiveSet())
            .isEqualTo(set2)
    }
}

