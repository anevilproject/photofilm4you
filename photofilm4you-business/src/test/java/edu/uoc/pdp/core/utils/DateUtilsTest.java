package edu.uoc.pdp.core.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeParseException;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class DateUtilsTest {

    @Test
    public void parseMustParseDate() {
        LocalDate date = DateUtils.parse("01/02/2021");

        assertEquals(1, date.getDayOfMonth());
        assertEquals(Month.FEBRUARY, date.getMonth());
        assertEquals(2021, date.getYear());
    }

    @Test(expected = DateTimeParseException.class)
    public void parseMustThrowExceptionOnInvalidFormat() {
        DateUtils.parse("01-02-2021");
    }

    @Test
    public void formatMustReturnFormattedDate() {
        LocalDate date = LocalDate.of(2021, 2, 1);

        String formatted = DateUtils.format(date);

        assertEquals("01/02/2021", formatted);
    }

    @Test
    public void formatPublishDateMustReturnFormattedDate() {
        LocalDateTime date = LocalDateTime.of(2021, 2, 1, 0, 0);

        String formatted = DateUtils.formatPublishDate(date);

        assertEquals("Publicat l'1 de febrer del 2021", formatted);
    }

    @Test
    public void formatCancellationDateMustReturnFormattedDate() {
        LocalDateTime date = LocalDateTime.of(2021, 8, 1, 0, 0);

        String formatted = DateUtils.formatCancellationDate(date);

        assertEquals("Cancel·lat l'1 d'agost del 2021", formatted);
    }

    @Test
    public void formatCreationDateMustReturnFormattedDate() {
        LocalDateTime date = LocalDateTime.of(2021, 4, 11, 0, 0);

        String formatted = DateUtils.formatCreationDate(date);

        assertEquals("Reservat l'11 d'abril del 2021", formatted);
    }
}
