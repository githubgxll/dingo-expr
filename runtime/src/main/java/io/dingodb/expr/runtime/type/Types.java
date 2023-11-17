/*
 * Copyright 2021 DataCanvas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.dingodb.expr.runtime.type;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class Types {
    public static final NullType NULL = new NullType();
    public static final IntType INT = new IntType();
    public static final LongType LONG = new LongType();
    public static final FloatType FLOAT = new FloatType();
    public static final DoubleType DOUBLE = new DoubleType();
    public static final BoolType BOOL = new BoolType();
    public static final DecimalType DECIMAL = new DecimalType();
    public static final StringType STRING = new StringType();
    public static final BytesType BYTES = new BytesType();
    public static final DateType DATE = new DateType();
    public static final TimeType TIME = new TimeType();
    public static final TimestampType TIMESTAMP = new TimestampType();
    public static final AnyType ANY = new AnyType();

    public static final ArrayType ARRAY_INT = new ArrayType(INT);
    public static final ArrayType ARRAY_LONG = new ArrayType(LONG);
    public static final ArrayType ARRAY_FLOAT = new ArrayType(FLOAT);
    public static final ArrayType ARRAY_DOUBLE = new ArrayType(DOUBLE);
    public static final ArrayType ARRAY_BOOL = new ArrayType(BOOL);
    public static final ArrayType ARRAY_DECIMAL = new ArrayType(DECIMAL);
    public static final ArrayType ARRAY_STRING = new ArrayType(STRING);
    public static final ArrayType ARRAY_BYTES = new ArrayType(BYTES);
    public static final ArrayType ARRAY_DATE = new ArrayType(DATE);
    public static final ArrayType ARRAY_TIME = new ArrayType(TIME);
    public static final ArrayType ARRAY_TIMESTAMP = new ArrayType(TIMESTAMP);
    public static final ArrayType ARRAY_ANY = new ArrayType(ANY);

    public static final ListType LIST_INT = new ListType(INT);
    public static final ListType LIST_LONG = new ListType(LONG);
    public static final ListType LIST_FLOAT = new ListType(FLOAT);
    public static final ListType LIST_DOUBLE = new ListType(DOUBLE);
    public static final ListType LIST_BOOL = new ListType(BOOL);
    public static final ListType LIST_DECIMAL = new ListType(DECIMAL);
    public static final ListType LIST_STRING = new ListType(STRING);
    public static final ListType LIST_BYTES = new ListType(BYTES);
    public static final ListType LIST_DATE = new ListType(DATE);
    public static final ListType LIST_TIME = new ListType(TIME);
    public static final ListType LIST_TIMESTAMP = new ListType(TIMESTAMP);
    public static final ListType LIST_ANY = new ListType(ANY);

    public static final MapType MAP = new MapType(ANY, ANY);

    private Types() {
    }

    public static ArrayType array(@NonNull Type elementType) {
        if (elementType.isScalar()) {
            return ArrayTypeBuilder.INSTANCE.visit(elementType);
        }
        return new ArrayType(elementType);
    }

    public static ListType list(@NonNull Type elementType) {
        if (elementType.isScalar()) {
            return ListTypeBuilder.INSTANCE.visit(elementType);
        }
        return new ListType(elementType);
    }

    public static @NonNull MapType map(@NonNull Type keyType, @NonNull Type valueType) {
        return new MapType(keyType, valueType);
    }

    public static @NonNull TupleType tuple(@NonNull Type[] types) {
        return new TupleType(types);
    }

    /**
     * Get the {@link Type} of a {@link Class}.
     * {@link List} stands for all its subtypes because they share the same operations,
     * and {@link Map} for all its subtypes.
     *
     * @param clazz the {@link Class}
     * @return the {@link Type}
     */
    public static Type classType(@NonNull Class<?> clazz) {
        if (clazz.isArray()) {
            // `byte[]` is looked on as a scalar clazz.
            if (byte[].class.isAssignableFrom(clazz)) {
                return BYTES;
            }
            return array(classType(clazz.getComponentType()));
        } else if (int.class.isAssignableFrom(clazz) || Integer.class.isAssignableFrom(clazz)) {
            return INT;
        } else if (long.class.isAssignableFrom(clazz) || Long.class.isAssignableFrom(clazz)) {
            return LONG;
        } else if (boolean.class.isAssignableFrom(clazz) || Boolean.class.isAssignableFrom(clazz)) {
            return BOOL;
        } else if (double.class.isAssignableFrom(clazz) || Double.class.isAssignableFrom(clazz)) {
            return DOUBLE;
        } else if (float.class.isAssignableFrom(clazz) || Float.class.isAssignableFrom(clazz)) {
            return FLOAT;
        } else if (BigDecimal.class.isAssignableFrom(clazz)) {
            return DECIMAL;
        } else if (String.class.isAssignableFrom(clazz)) {
            return STRING;
        } else if (Date.class.isAssignableFrom(clazz)) {
            return DATE;
        } else if (Time.class.isAssignableFrom(clazz)) {
            return TIME;
        } else if (Timestamp.class.isAssignableFrom(clazz)) {
            return TIMESTAMP;
        } else if (List.class.isAssignableFrom(clazz)) {
            return LIST_ANY;
        } else if (Map.class.isAssignableFrom(clazz)) {
            return MAP;
        } else if (void.class.isAssignableFrom(clazz) || Void.class.isAssignableFrom(clazz)) {
            return NULL;
        }
        return ANY;
    }

    /**
     * Get the {@link Type} of an {@link Object} by get its class first.
     *
     * @param value the {@link Object}
     * @return the {@link Type}
     */
    public static Type valueType(Object value) {
        if (value == null) {
            return NULL;
        }
        return classType(value.getClass());
    }

    public static @Nullable Type bestType(@NonNull Type @NonNull ... types) {
        Type best = NULL;
        for (Type type : types) {
            best = (type.numericPrecedence() > best.numericPrecedence() ? type : best);
        }
        if (best.numericPrecedence() != Type.NOT_NUMERIC) {
            return (!best.equals(NULL) && !best.equals(BOOL)) ? best : INT;
        }
        Type finalBest = best;
        if (Arrays.stream(types).allMatch(t -> t.equals(finalBest) || t.equals(NULL))) {
            return best;
        }
        return null;
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static class ArrayTypeBuilder extends TypeVisitorBase<ArrayType, Void> {
        private static final ArrayTypeBuilder INSTANCE = new ArrayTypeBuilder();

        @Override
        public ArrayType visitIntType(@NonNull IntType type, Void obj) {
            return ARRAY_INT;
        }

        @Override
        public ArrayType visitLongType(@NonNull LongType type, Void obj) {
            return ARRAY_LONG;
        }

        @Override
        public ArrayType visitFloatType(@NonNull FloatType type, Void obj) {
            return ARRAY_FLOAT;
        }

        @Override
        public ArrayType visitDoubleType(@NonNull DoubleType type, Void obj) {
            return ARRAY_DOUBLE;
        }

        @Override
        public ArrayType visitBoolType(@NonNull BoolType type, Void obj) {
            return ARRAY_BOOL;
        }

        @Override
        public ArrayType visitDecimalType(@NonNull DecimalType type, Void obj) {
            return ARRAY_DECIMAL;
        }

        @Override
        public ArrayType visitStringType(@NonNull StringType type, Void obj) {
            return ARRAY_STRING;
        }

        @Override
        public ArrayType visitBytesType(@NonNull BytesType type, Void obj) {
            return ARRAY_BYTES;
        }

        @Override
        public ArrayType visitDateType(@NonNull DateType type, Void obj) {
            return ARRAY_DATE;
        }

        @Override
        public ArrayType visitTimeType(@NonNull TimeType type, Void obj) {
            return ARRAY_TIME;
        }

        @Override
        public ArrayType visitTimestampType(@NonNull TimestampType type, Void obj) {
            return ARRAY_TIMESTAMP;
        }

        @Override
        public ArrayType visitAnyType(@NonNull AnyType type, Void obj) {
            return ARRAY_ANY;
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static class ListTypeBuilder extends TypeVisitorBase<ListType, Void> {
        private static final ListTypeBuilder INSTANCE = new ListTypeBuilder();

        @Override
        public ListType visitIntType(@NonNull IntType type, Void obj) {
            return LIST_INT;
        }

        @Override
        public ListType visitLongType(@NonNull LongType type, Void obj) {
            return LIST_LONG;
        }

        @Override
        public ListType visitFloatType(@NonNull FloatType type, Void obj) {
            return LIST_FLOAT;
        }

        @Override
        public ListType visitDoubleType(@NonNull DoubleType type, Void obj) {
            return LIST_DOUBLE;
        }

        @Override
        public ListType visitBoolType(@NonNull BoolType type, Void obj) {
            return LIST_BOOL;
        }

        @Override
        public ListType visitDecimalType(@NonNull DecimalType type, Void obj) {
            return LIST_DECIMAL;
        }

        @Override
        public ListType visitStringType(@NonNull StringType type, Void obj) {
            return LIST_STRING;
        }

        @Override
        public ListType visitBytesType(@NonNull BytesType type, Void obj) {
            return LIST_BYTES;
        }

        @Override
        public ListType visitDateType(@NonNull DateType type, Void obj) {
            return LIST_DATE;
        }

        @Override
        public ListType visitTimeType(@NonNull TimeType type, Void obj) {
            return LIST_TIME;
        }

        @Override
        public ListType visitTimestampType(@NonNull TimestampType type, Void obj) {
            return LIST_TIMESTAMP;
        }

        @Override
        public ListType visitAnyType(@NonNull AnyType type, Void obj) {
            return LIST_ANY;
        }
    }
}
