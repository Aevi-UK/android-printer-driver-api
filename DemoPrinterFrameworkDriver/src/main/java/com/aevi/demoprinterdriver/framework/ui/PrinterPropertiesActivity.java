/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aevi.demoprinterdriver.framework.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

import com.aevi.demoprinterdriver.framework.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PrinterPropertiesActivity extends AppCompatActivity {

    private static final String EXTRA_PRINTER_ID = "printerId";

    @BindView(R.id.printer_id)
    TextView printerId;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer_properties);
        ButterKnife.bind(this);

        printerId.setText(getIntent().getStringExtra(EXTRA_PRINTER_ID));
    }
}
