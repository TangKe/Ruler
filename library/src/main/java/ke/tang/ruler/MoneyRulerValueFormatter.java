/*
 * Copyright (C) 2018 TangKe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ke.tang.ruler;

import java.text.DecimalFormat;

/**
 * Created by TangKe on 2017/3/24.
 */

public class MoneyRulerValueFormatter implements RulerValueFormatter {
    private int mMultiple = 500;
    private int mInitialValue = 0;
    private DecimalFormat mDecimalFormat = new DecimalFormat(",###");

    public MoneyRulerValueFormatter() {
    }

    public MoneyRulerValueFormatter(int multiple, int initialValue) {
        mMultiple = multiple;
        mInitialValue = initialValue;
    }

    @Override
    public String formatValue(int value) {
        return mDecimalFormat.format(mInitialValue + value * mMultiple);
    }
}
