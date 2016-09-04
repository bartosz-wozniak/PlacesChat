package com.android.wut.placereviewer;

import com.android.wut.placereviewer.utils.HashUtils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;

/**
 * Created by soive on 16.06.2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class HashTest {

    @Test
    public void testHashing() {
        String pass = "aaaaa";
        String result = HashUtils.HashText(pass);
        assertThat(result, is("ed968e840d10d2d313a870bc131a4e2c311d7ad09bdf32b3418147221f51a6e2"));
    }


}
