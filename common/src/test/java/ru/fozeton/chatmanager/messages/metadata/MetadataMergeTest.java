package ru.fozeton.chatmanager.messages.metadata;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MetadataMergeTest {
    record Meta(String channel, String lineColor, String borderColor) implements MetadataType {}

    record Other(String name) implements MetadataType {}

    record WithPrimitives(String name, int count, boolean flag) implements MetadataType {}

    record WithStatic(String name) implements MetadataType {
        static final String CONSTANT = "x";
    }

    @Test
    void newValuesOverrideOld() {
        Meta old = new Meta("s", "s", "s");
        MetadataType result = old.merge(new Meta("aboba", "jojo", "kko"));

        assertEquals(new Meta("aboba", "jojo", "kko"), result);
    }

    @Test
    void originalIsNotMutated() {
        Meta old = new Meta("s", "s", "s");
        old.merge(new Meta("aboba", "jojo", "kko"));

        assertEquals("s", old.channel());
        assertEquals("s", old.lineColor());
        assertEquals("s", old.borderColor());
    }

    @Test
    void nullFieldsKeepOldValues() {
        Meta old = new Meta("ch", "red", "blue");
        MetadataType result = old.merge(new Meta(null, "green", null));

        assertEquals(new Meta("ch", "green", "blue"), result);
    }

    @Test
    void allNullFieldsChangeNothing() {
        Meta old = new Meta("ch", "red", "blue");
        MetadataType result = old.merge(new Meta(null, null, null));

        assertEquals(old, result);
    }

    @Test
    void oldNullFieldsAreFilledFromNew() {
        Meta old = new Meta(null, null, null);
        MetadataType result = old.merge(new Meta("ch", "red", "blue"));

        assertEquals(new Meta("ch", "red", "blue"), result);
    }

    @Test
    void bothNullStaysNull() {
        Meta old = new Meta("ch", null, null);
        Meta result = (Meta) old.merge(new Meta(null, null, "blue"));

        assertEquals("ch", result.channel());
        assertNull(result.lineColor());
        assertEquals("blue", result.borderColor());
    }

    @Test
    void nullArgumentReturnsThis() {
        Meta old = new Meta("s", "s", "s");

        assertSame(old, old.merge(null));
    }

    @Test
    void differentTypeReturnsNewValue() {
        Meta old = new Meta("s", "s", "s");
        Other other = new Other("x");

        assertSame(other, old.merge(other));
    }

    @Test
    void mergeWithItselfGivesEqualObject() {
        Meta meta = new Meta("a", "b", "c");

        assertEquals(meta, meta.merge(meta));
    }

    @Test
    void chainedMergesAccumulate() {
        MetadataType result = new Meta("a", null, null)
                .merge(new Meta(null, "b", null))
                .merge(new Meta(null, null, "c"));

        assertEquals(new Meta("a", "b", "c"), result);
    }

    @Test
    void lastMergeWins() {
        MetadataType result = new Meta("a", "a", "a")
                .merge(new Meta("b", "b", "b"))
                .merge(new Meta("c", null, "c"));

        assertEquals(new Meta("c", "b", "c"), result);
    }

    @Test
    void primitivesAreHandled() {
        WithPrimitives old = new WithPrimitives("old", 1, false);
        MetadataType result = old.merge(new WithPrimitives("new", 5, true));

        assertEquals(new WithPrimitives("new", 5, true), result);
    }

    @Test
    void staticFieldsAreIgnored() {
        WithStatic old = new WithStatic("a");
        MetadataType result = old.merge(new WithStatic("b"));

        assertEquals(new WithStatic("b"), result);
    }

    @Test
    void mapMergeStoresResult() {
        Map<Class<?>, MetadataType> map = new HashMap<>();

        MetadataType first = new Meta("a", "b", null);
        MetadataType second = new Meta(null, "x", "c");

        map.merge(first.getClass(), first, MetadataType::merge);
        assertSame(first, map.get(Meta.class));

        map.merge(second.getClass(), second, MetadataType::merge);
        assertEquals(new Meta("a", "x", "c"), map.get(Meta.class));
    }

    @Test
    void mapKeepsDifferentTypesSeparately() {
        Map<Class<?>, MetadataType> map = new HashMap<>();

        MetadataType meta = new Meta("a", "b", "c");
        MetadataType other = new Other("n");

        map.merge(meta.getClass(), meta, MetadataType::merge);
        map.merge(other.getClass(), other, MetadataType::merge);

        assertEquals(2, map.size());
        assertEquals(meta, map.get(Meta.class));
        assertEquals(other, map.get(Other.class));
    }
}