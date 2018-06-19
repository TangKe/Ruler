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
package me.tangke.ruler;

/**
 * 用于格式化显示标尺的值
 * 标尺值只允许是从0到{@link RulerView#MAX_VALUE}之间的数值，如果需要展示成其它文本，就需要实现本接口，在{@link #formatValue(int)}方法中实现值与文本对应关系
 * 例如：
 * <table>
 *     <th>
 *         <td>值</td>
 *         <td>文本</td>
 *     </th>
 *     <tr>
 *         <td>0</td>
 *         <td>零</td>
 *     </tr>
 *     <tr>
 *         <td>10</td>
 *         <td>十</td>
 *     </tr>
 * </table>
 */
public interface RulerValueFormatter {
    /**
     * 当标尺需要显示一个值的时候调用该方法获取格式化后的值
     *
     * @param value 当前的值，取值范围从0到{@link RulerView#MAX_VALUE}
     * @return 当前值对应的文本
     */
    String formatValue(int value);
}