package com.backend.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FibonacciHeapTest {

    private FibonacciHeap<String> heap;

    @BeforeEach
    void setUp() {
        heap = new FibonacciHeap<>();
    }

    @Test
    void constructor_ShouldInitializeEmptyHeap() {
        // When & Then
        assertTrue(heap.isEmpty());
        assertEquals(0, heap.size());
    }

    @Test
    void insert_SingleElement_ShouldUpdateSizeAndMin() {
        // When
        heap.insert("element1", 5.0);

        // Then
        assertFalse(heap.isEmpty());
        assertEquals(1, heap.size());
    }

    @Test
    void insert_MultipleElements_ShouldMaintainHeapProperty() {
        // Given
        heap.insert("element1", 10.0);
        heap.insert("element2", 5.0);
        heap.insert("element3", 15.0);

        // Then
        assertEquals(3, heap.size());
        assertEquals("element2", heap.extractMin()); // Should extract minimum priority
    }

    @Test
    void insert_ElementsWithSamePriority_ShouldHandleCorrectly() {
        // Given
        heap.insert("element1", 5.0);
        heap.insert("element2", 5.0);
        heap.insert("element3", 5.0);

        // Then
        assertEquals(3, heap.size());
        
        // Extract all elements to verify they exist
        String first = heap.extractMin();
        String second = heap.extractMin();
        String third = heap.extractMin();
        
        assertNotNull(first);
        assertNotNull(second);
        assertNotNull(third);
        assertTrue(heap.isEmpty());
    }

    @Test
    void extractMin_EmptyHeap_ShouldReturnNull() {
        // When
        String result = heap.extractMin();

        // Then
        assertNull(result);
        assertTrue(heap.isEmpty());
    }

    @Test
    void extractMin_SingleElement_ShouldReturnElementAndEmptyHeap() {
        // Given
        heap.insert("onlyElement", 10.0);

        // When
        String result = heap.extractMin();

        // Then
        assertEquals("onlyElement", result);
        assertTrue(heap.isEmpty());
        assertEquals(0, heap.size());
    }

    @Test
    void extractMin_MultipleElements_ShouldReturnMinimumPriorityElement() {
        // Given
        heap.insert("high", 20.0);
        heap.insert("medium", 10.0);
        heap.insert("low", 5.0);
        heap.insert("highest", 25.0);

        // When
        String first = heap.extractMin();
        String second = heap.extractMin();
        String third = heap.extractMin();
        String fourth = heap.extractMin();

        // Then
        assertEquals("low", first);      // Priority 5.0
        assertEquals("medium", second);   // Priority 10.0
        assertEquals("high", third);     // Priority 20.0
        assertEquals("highest", fourth); // Priority 25.0
        assertTrue(heap.isEmpty());
    }

    @Test
    void extractMin_AfterMultipleExtractions_ShouldMaintainHeapProperty() {
        // Given
        for (int i = 10; i >= 1; i--) {
            heap.insert("element" + i, i);
        }

        // When & Then
        for (int i = 1; i <= 10; i++) {
            String extracted = heap.extractMin();
            assertEquals("element" + i, extracted);
            assertEquals(10 - i, heap.size());
        }
    }

    @Test
    void size_EmptyHeap_ShouldReturnZero() {
        // Then
        assertEquals(0, heap.size());
    }

    @Test
    void size_AfterInsertions_ShouldReturnCorrectSize() {
        // Given
        heap.insert("element1", 1.0);
        assertEquals(1, heap.size());

        heap.insert("element2", 2.0);
        assertEquals(2, heap.size());

        heap.insert("element3", 3.0);
        assertEquals(3, heap.size());
    }

    @Test
    void size_AfterExtractions_ShouldDecrease() {
        // Given
        heap.insert("element1", 1.0);
        heap.insert("element2", 2.0);
        heap.insert("element3", 3.0);

        // When
        heap.extractMin();
        assertEquals(2, heap.size());

        heap.extractMin();
        assertEquals(1, heap.size());

        heap.extractMin();
        assertEquals(0, heap.size());
    }

    @Test
    void isEmpty_NewHeap_ShouldReturnTrue() {
        // Then
        assertTrue(heap.isEmpty());
    }

    @Test
    void isEmpty_AfterInsertions_ShouldReturnFalse() {
        // Given
        heap.insert("element", 1.0);

        // Then
        assertFalse(heap.isEmpty());
    }

    @Test
    void isEmpty_AfterExtractingAllElements_ShouldReturnTrue() {
        // Given
        heap.insert("element1", 1.0);
        heap.insert("element2", 2.0);

        // When
        heap.extractMin();
        heap.extractMin();

        // Then
        assertTrue(heap.isEmpty());
    }

    @Test
    void extractTopN_EmptyHeap_ShouldReturnEmptyList() {
        // When
        List<String> result = heap.extractTopN(5);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void extractTopN_RequestMoreThanAvailable_ShouldReturnAllElements() {
        // Given
        heap.insert("element1", 3.0);
        heap.insert("element2", 1.0);
        heap.insert("element3", 2.0);

        // When
        List<String> result = heap.extractTopN(10);

        // Then
        assertEquals(3, result.size());
        assertEquals("element2", result.get(0)); // Priority 1.0
        assertEquals("element3", result.get(1)); // Priority 2.0
        assertEquals("element1", result.get(2)); // Priority 3.0
        assertTrue(heap.isEmpty());
    }

    @Test
    void extractTopN_RequestPartialElements_ShouldReturnTopN() {
        // Given
        heap.insert("element1", 5.0);
        heap.insert("element2", 1.0);
        heap.insert("element3", 3.0);
        heap.insert("element4", 4.0);
        heap.insert("element5", 2.0);

        // When
        List<String> result = heap.extractTopN(3);

        // Then
        assertEquals(3, result.size());
        assertEquals("element2", result.get(0)); // Priority 1.0
        assertEquals("element5", result.get(1)); // Priority 2.0
        assertEquals("element3", result.get(2)); // Priority 3.0
        assertEquals(2, heap.size()); // Should have 2 elements left
    }

    @Test
    void extractTopN_ZeroElements_ShouldReturnEmptyList() {
        // Given
        heap.insert("element", 1.0);

        // When
        List<String> result = heap.extractTopN(0);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(1, heap.size()); // Heap should remain unchanged
    }

    @Test
    void extractTopN_NegativeN_ShouldReturnEmptyList() {
        // Given
        heap.insert("element", 1.0);

        // When
        List<String> result = heap.extractTopN(-1);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(1, heap.size()); // Heap should remain unchanged
    }

    @Test
    void heapOperations_LargeNumberOfElements_ShouldMaintainPerformance() {
        // Given
        int numElements = 1000;
        List<Integer> expectedOrder = new ArrayList<>();
        
        // Insert elements in reverse order
        for (int i = numElements; i >= 1; i--) {
            heap.insert("element" + i, i);
            expectedOrder.add(i);
        }

        // When & Then
        assertEquals(numElements, heap.size());
        
        for (int i = 1; i <= numElements; i++) {
            String extracted = heap.extractMin();
            assertEquals("element" + i, extracted);
        }
        
        assertTrue(heap.isEmpty());
    }

    @Test
    void heapOperations_WithDuplicatePriorities_ShouldHandleCorrectly() {
        // Given
        heap.insert("a1", 1.0);
        heap.insert("b2", 2.0);
        heap.insert("a2", 1.0);  // Duplicate priority
        heap.insert("c3", 3.0);
        heap.insert("b1", 2.0);  // Duplicate priority

        // When
        List<String> extracted = new ArrayList<>();
        while (!heap.isEmpty()) {
            extracted.add(heap.extractMin());
        }

        // Then
        assertEquals(5, extracted.size());
        
        // First two should have priority 1.0
        assertTrue(extracted.get(0).startsWith("a"));
        assertTrue(extracted.get(1).startsWith("a"));
        
        // Next two should have priority 2.0
        assertTrue(extracted.get(2).startsWith("b"));
        assertTrue(extracted.get(3).startsWith("b"));
        
        // Last should have priority 3.0
        assertEquals("c3", extracted.get(4));
    }

    @Test
    void heapOperations_InterleavedInsertAndExtract_ShouldMaintainCorrectness() {
        // Given & When
        heap.insert("element1", 5.0);
        heap.insert("element2", 2.0);
        
        String first = heap.extractMin(); // Should be element2
        
        heap.insert("element3", 1.0);
        heap.insert("element4", 8.0);
        
        String second = heap.extractMin(); // Should be element3
        String third = heap.extractMin();  // Should be element1
        String fourth = heap.extractMin(); // Should be element4

        // Then
        assertEquals("element2", first);
        assertEquals("element3", second);
        assertEquals("element1", third);
        assertEquals("element4", fourth);
        assertTrue(heap.isEmpty());
    }

    @Test
    void heapOperations_WithNegativePriorities_ShouldHandleCorrectly() {
        // Given
        heap.insert("negative", -5.0);
        heap.insert("positive", 5.0);
        heap.insert("zero", 0.0);
        heap.insert("moreNegative", -10.0);

        // When
        String first = heap.extractMin();
        String second = heap.extractMin();
        String third = heap.extractMin();
        String fourth = heap.extractMin();

        // Then
        assertEquals("moreNegative", first);  // -10.0
        assertEquals("negative", second);     // -5.0
        assertEquals("zero", third);          // 0.0
        assertEquals("positive", fourth);     // 5.0
    }

    @Test
    void heapOperations_WithComplexObjectTypes_ShouldWork() {
        // Given
        FibonacciHeap<Integer> intHeap = new FibonacciHeap<>();
        
        intHeap.insert(100, 3.0);
        intHeap.insert(200, 1.0);
        intHeap.insert(300, 2.0);

        // When
        Integer first = intHeap.extractMin();
        Integer second = intHeap.extractMin();
        Integer third = intHeap.extractMin();

        // Then
        assertEquals(Integer.valueOf(200), first);
        assertEquals(Integer.valueOf(300), second);
        assertEquals(Integer.valueOf(100), third);
    }

    @Test
    void consolidate_AfterExtractMin_ShouldMergeTreesCorrectly() {
        // Given - Insert many elements to trigger consolidation
        heap.insert("a", 1.0);
        heap.insert("b", 2.0);
        heap.insert("c", 3.0);
        heap.insert("d", 4.0);
        heap.insert("e", 5.0);
        heap.insert("f", 6.0);
        heap.insert("g", 7.0);
        heap.insert("h", 8.0);

        // When - Extract minimum to trigger consolidation
        String min = heap.extractMin();

        // Then
        assertEquals("a", min);
        assertEquals(7, heap.size());
        
        // Verify heap property is maintained
        String nextMin = heap.extractMin();
        assertEquals("b", nextMin);
    }
} 