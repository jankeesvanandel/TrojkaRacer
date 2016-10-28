package nl.jpoint.trojkaracer.ai;

import nl.jpoint.trojkaracer.processing.ProcessingService;
import nl.jpoint.trojkaracer.processing.TrackBoundaries;
import nl.jpoint.trojkaracer.processing.TrackInfo;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@Ignore
public class AIServiceImplTest {

    private int[][] inputData = new int[][] {
        new int[]{282, 0, 420},
        new int[]{281, 0, 419},
        new int[]{280, 0, 425},
        new int[]{279, 0, 427},
        new int[]{278, 0, 428},
        new int[]{277, 0, 429},
        new int[]{276, 0, 430},
        new int[]{275, 0, 432},
        new int[]{274, 0, 432},
        new int[]{273, 0, 434},
        new int[]{272, 0, 435},
        new int[]{271, 0, 436},
        new int[]{270, 0, 437},
        new int[]{269, 0, 439},
        new int[]{268, 0, 441},
        new int[]{267, 0, 443},
        new int[]{266, 0, 445},
        new int[]{265, 0, 447},
        new int[]{264, 0, 448},
        new int[]{263, 0, 450},
        new int[]{262, 0, 452},
        new int[]{261, 0, 454},
        new int[]{260, 0, 456},
        new int[]{259, 0, 457},
        new int[]{258, 0, 458},
        new int[]{257, 0, 460},
        new int[]{256, 4, 460},
        new int[]{255, 5, 420},
        new int[]{254, 6, 419},
        new int[]{253, 9, 464},
        new int[]{252, 11, 467},
        new int[]{251, 13, 469},
        new int[]{250, 15, 471},
        new int[]{249, 16, 474},
        new int[]{248, 17, 476},
        new int[]{247, 19, 477},
        new int[]{246, 22, 479},
        new int[]{245, 24, 481},
        new int[]{244, 26, 484},
        new int[]{243, 29, 487},
        new int[]{242, 33, 490},
        new int[]{241, 38, 496},
        new int[]{240, 39, 496},
        new int[]{239, 40, 496},
        new int[]{238, 40, 496},
        new int[]{237, 41, 496},
        new int[]{236, 43, 496},
        new int[]{235, 45, 496},
        new int[]{234, 48, 496},
        new int[]{233, 50, 496},
        new int[]{232, 51, 496},
        new int[]{231, 53, 496},
        new int[]{230, 56, 496},
        new int[]{229, 58, 496},
        new int[]{228, 59, 496},
        new int[]{227, 61, 496},
        new int[]{226, 63, 496},
        new int[]{225, 65, 496},
        new int[]{224, 66, 496},
        new int[]{223, 68, 496},
        new int[]{222, 70, 496},
        new int[]{221, 72, 496},
        new int[]{220, 75, 496},
        new int[]{219, 79, 496},
        new int[]{218, 80, 496},
        new int[]{217, 81, 496},
        new int[]{216, 85, 496},
        new int[]{215, 88, 496},
        new int[]{214, 92, 496},
        new int[]{213, 94, 496},
        new int[]{212, 96, 496},
        new int[]{211, 99, 496},
        new int[]{210, 101, 496},
        new int[]{209, 102, 496},
        new int[]{208, 107, 496},
        new int[]{207, 111, 496},
        new int[]{206, 122, 496},
        new int[]{205, 125, 496},
        new int[]{204, 127, 496},
        new int[]{203, 130, 496},
        new int[]{202, 132, 496},
        new int[]{201, 134, 496},
        new int[]{200, 137, 496},
        new int[]{199, 140, 496},
        new int[]{198, 143, 496},
        new int[]{197, 145, 496},
        new int[]{196, 148, 496},
        new int[]{195, 150, 496},
        new int[]{194, 153, 496},
        new int[]{193, 156, 496},
        new int[]{192, 159, 496},
        new int[]{191, 163, 496},
        new int[]{190, 167, 496},
        new int[]{189, 171, 496},
        new int[]{188, 176, 496},
        new int[]{187, 182, 496},
        new int[]{186, 184, 496},
        new int[]{185, 187, 496},
        new int[]{184, 190, 496},
        new int[]{183, 194, 496},
        new int[]{182, 200, 496},
        new int[]{181, 208, 496},
        new int[]{180, 217, 496},
        new int[]{179, 222, 496},
        new int[]{178, 243, 496},
        new int[]{177, 252, 496},
        new int[]{176, 256, 496},
        new int[]{175, 260, 496},
        new int[]{174, 270, 496},
        new int[]{173, 276, 496},
        new int[]{172, 281, 496},
        new int[]{171, 288, 496},
        new int[]{170, 296, 496},
        new int[]{169, 302, 496},
        new int[]{168, 308, 496},
        new int[]{167, 311, 496},
        new int[]{166, 316, 496},
        new int[]{165, 324, 496},
        new int[]{164, 351, 496},
        new int[]{163, 355, 496},
        new int[]{162, 382, 496},
        new int[]{161, 392, 496},
        new int[]{160, 422, 496},
        new int[]{159, 432, 496},
        new int[]{158, 440, 496},
        new int[]{157, 450, 496}
    };

    @Mock
    private ProcessingService processingServiceMock;

    @Mock
    private TrackInfo trackInfoMock;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void empty() {
        DesiredActions result = runTest(
        );

        assertThat(result.getSteeringAction().getSteeringPosition(), is(0.0));
        assertThat(result.getThrottleAction().getThrottleAmount(), is(0.0));
    }

    @Test
    public void simplest() {
        DesiredActions result = runTest(
                trackSlice(0)
        );

        assertThat(result.getSteeringAction().getSteeringPosition(), is(0.0));
        assertThat(result.getThrottleAction().getThrottleAmount(), is(0.025));
    }

    @Test
    public void simple() {
        DesiredActions result = runTest(
                new int[]{0, 100},
                new int[]{0, 100}
        );

        assertThat(result.getSteeringAction().getSteeringPosition(), is(0.0));
        assertThat(result.getThrottleAction().getThrottleAmount(), is(0.05));
    }

    @Test
    public void cornerLeft() {
        DesiredActions result = runTest(
                trackSlice(100),
                trackSlice(90),
                trackSlice(80),
                trackSlice(70),
                trackSlice(60),
                trackSlice(50),
                trackSlice(40),
                trackSlice(30),
                trackSlice(20),
                trackSlice(10),
                trackSlice(0)
        );

        assertThat(result.getSteeringAction().getSteeringPosition(), is(-0.9072900935107435));
        assertThat(result.getThrottleAction().getThrottleAmount(), is(0.275));
    }

    @Test
    public void cornerRight() {
        DesiredActions result = runTest(
                trackSlice(0),
                trackSlice(10),
                trackSlice(20),
                trackSlice(30),
                trackSlice(40),
                trackSlice(50),
                trackSlice(60),
                trackSlice(70),
                trackSlice(80),
                trackSlice(90),
                trackSlice(100)
        );

        assertThat(result.getSteeringAction().getSteeringPosition(), is(0.9072900935107435));
        assertThat(result.getThrottleAction().getThrottleAmount(), is(0.275));
    }

    @Test
    public void advanced() {
        DesiredActions result = runTest(inputData);

        assertThat(result.getSteeringAction().getSteeringPosition(), is(0.3690583499304141));
        assertThat(result.getThrottleAction().getThrottleAmount(), is(1.0));
    }

    @Test
    public void testWithABitOfLoad() {
        for (int i = 0; i < 1000; i++) {
            cornerLeft();
            cornerRight();
            advanced();
        }
    }

    private static int[] trackSlice(final int position) {
        return new int[]{position, position + 100};
    }

    private DesiredActions runTest(int[]... data) {
        AIService aiService = new AIServiceImpl(processingServiceMock);
        when(processingServiceMock.getTrackInfo()).thenReturn(trackInfoMock);
        when(trackInfoMock.getBoundaries()).thenReturn(new TrackBoundaries(
                Arrays.asList(data)
        ));
        when(trackInfoMock.getTimestamp()).thenReturn(1L);

        return aiService.getDesiredActions();
    }
}