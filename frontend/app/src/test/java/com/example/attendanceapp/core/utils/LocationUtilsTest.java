package com.example.attendanceapp.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class LocationUtilsTest {

    @Test
    public void testStripCountryOrLastSegment_removesCountry() {
        String input = "123 Đường Cầu Giấy, Quan Hoa, Cầu Giấy, Hà Nội, Việt Nam";
        String expected = "123 Đường Cầu Giấy, Quan Hoa, Cầu Giấy, Hà Nội";
        assertEquals(expected, LocationUtils.stripCountryOrLastSegment(input));
    }

    @Test
    public void testStripCountryOrLastSegment_removesVietnamEnglish() {
        String input = "123 Nguyen Trai, Thanh Xuan, Ha Noi, Vietnam";
        String expected = "123 Nguyen Trai, Thanh Xuan, Ha Noi";
        assertEquals(expected, LocationUtils.stripCountryOrLastSegment(input));
    }

    @Test
    public void testStripCountryOrLastSegment_withTrailingCommaAndDot() {
        String input = "12 Đại Cồ Việt, Hai Bà Trưng, Hà Nội, Việt Nam,. ";
        String expected = "12 Đại Cồ Việt, Hai Bà Trưng, Hà Nội";
        assertEquals(expected, LocationUtils.stripCountryOrLastSegment(input));
    }

    @Test
    public void testStripCountryOrLastSegment_singleSegment_returnsOriginal() {
        String input = "Hà Nội";
        assertEquals("Hà Nội", LocationUtils.stripCountryOrLastSegment(input));
    }

    @Test
    public void testStripCountryOrLastSegment_nullOrEmpty() {
        assertNull(LocationUtils.stripCountryOrLastSegment(null));
        assertEquals("", LocationUtils.stripCountryOrLastSegment(""));
        assertEquals("", LocationUtils.stripCountryOrLastSegment("   "));
    }
}
