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

package io.dingodb.expr.runtime.op.date;

import io.dingodb.expr.annotations.Operators;
import io.dingodb.expr.runtime.ExprConfig;
import io.dingodb.expr.runtime.op.UnaryOp;
import io.dingodb.expr.runtime.utils.DateTimeUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.sql.Date;
import java.sql.Timestamp;

@Operators
abstract class YearFun extends UnaryOp {
    public static final String NAME = "YEAR";
    private static final long serialVersionUID = 3443110114993243173L;

    static int extractYear(@NonNull Date value, @NonNull ExprConfig config) {
        return DateTimeUtils.extractYear(value);
    }

    static int extractYear(@NonNull Timestamp value, @NonNull ExprConfig config) {
        return DateTimeUtils.extractYear(value);
    }

    @Override
    public @NonNull String getName() {
        return NAME;
    }

}
