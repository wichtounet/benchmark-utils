package com.wicht.benchmark.utils;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import bb.science.FormatUtil;
import bb.science.Prefix;
import bb.util.Benchmark;
import bb.util.Benchmark.Params;

/**
 * A class representing a benchmark suite. The basic usage for this class is really simple :
 * <p/>
 * <blockquote><pre>
 *      Benchs benchs = new Benchs("Title of the benchmark");
 * <p/>
 *      benchs.bench("First method", new Runnable(){
 *          public void run() {
 *              method1();
 *          }
 *      });
 * <p/>
 *      benchs.bench("Second method", new Runnable(){
 *          public void run() {
 *              method2();
 *          }
 *      });
 * <p/>
 *      benchs.generateCharts();
 * </pre></blockquote>
 * <p/>
 * You can also use Callable<T> instead of Runnable. The charts will be generated in the current folder or in the
 * specified folder using the setFolder() method.
 * <p/>
 * Here are the default values of a benchmark suite : - Graph folder : user directory - Width of the graph : 500 -
 * Height of the graph : 400 - Exclusion factor : 50
 *
 * @author Baptiste Wicht
 * @version 1.0
 */
public class Benchs {
    private final List<NamedBenchmark> benchmarks = new ArrayList<NamedBenchmark>();

    private final String title;

    private String folder = System.getProperty("user.dir");
    private final Params params = new Params();
    private int width = 500;
    private int height = 400;
    private double factor = 50;
    private boolean console = true;

    /**
     * Create a new benchmark suite with the given title.
     *
     * @param title The title of the benchmark.
     */
    public Benchs(String title) {
        super();

        this.title = title;

        params.setConsoleFeedback(false);
        params.setEstimateNoiseFloor(true);
    }

    /**
     * Set the folder to create image into.
     *
     * @param folder The folder to create the image charts into.
     */
    public void setFolder(String folder) {
        if (!folder.endsWith("/")) {
            folder = folder + '/';
        }

        this.folder = folder;
    }

    /**
     * Specify the dimensions of the graph.
     *
     * @param width  The width of the graph.
     * @param height The height of the graph.
     */
    public void setGraphDimension(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Set the exclusion factor for the sub graph. The exclusion factor enable to remove several benchmarks results from
     * the graph to create a sub graph with only interesting results. All the results with a mean higher than
     * exclusionFactor times an other mean will be excluded from the sub graph.
     *
     * @param factor The exclusion factor.
     */
    public void setExclusionFactor(double factor) {
        if (factor < 1) {
            throw new IllegalArgumentException("The Exclusion factor cannot be less than 1. ");
        }

        this.factor = factor;
    }

    /**
     * Return the Benchmark params.
     *
     * @return The Benchmark params.
     */
    public Params getParams() {
        return params;
    }

    /**
     * Set if the results of the benchmark must be displayed on the console or not.
     *
     * @param console A boolean tag indicating if the results must be displayed on the console ({@code true}) or not
     *                ({@code false}).
     */
    public void setConsoleResults(boolean console) {
        this.console = console;
    }

    /**
     * Bench the given Runnable.
     *
     * @param name     The name of the tested method.
     * @param runnable The runnable to test.
     *
     * @see java.lang.Runnable
     */
    public void bench(String name, Runnable runnable) {
        Benchmark benchmark = new Benchmark(runnable, params);

        addBenchmarkResults(name, benchmark);
    }

    /**
     * Bench the given Callable.
     *
     * @param name     The name of the tested method.
     * @param callable The callable to test.
     * @param <T>      The type of result.
     *
     * @return The result of the callable.
     *
     * @throws Exception If the callable throws an exception in its call() method.
     * @see java.util.concurrent.Callable
     */
    public <T> T bench(String name, Callable<T> callable) throws Exception {
        Benchmark benchmark = new Benchmark(callable, params);

        addBenchmarkResults(name, benchmark);

        return (T) benchmark.getCallResult();
    }

    /**
     * Add benchmark results to the bench suite.
     *
     * @param name      The name of the tested method.
     * @param benchmark The Benchmark.
     */
    private void addBenchmarkResults(String name, Benchmark benchmark) {
        if (console) {
            System.out.println(name + " results : " + FormatUtil.toEngineeringTime(benchmark.getMean(), 3));
            System.out.println(benchmark.toStringFull());
        }

        benchmarks.add(new NamedBenchmark(benchmark, name));
    }

    /**
     * Generate the charts of the results. This method generate a sub chart if it's interesting. The time prefix will be
     * automatically computed from the results.
     */
    public void generateCharts() {
        generateCharts(true);
    }

    /**
     * Generate the charts of the results. The time prefix will be automatically computed from the results.
     *
     * @param subCharts A boolean tag indicating if we want to generate sub chart ({@code true}) or not ({@code
     *                  false}).
     */
    public void generateCharts(boolean subCharts) {
        generateCharts(computeOptimalPrefix(benchmarks), subCharts);
    }

    /**
     * Generate the charts using the given time prefix. This method generate a sub chart if it's interesting.
     *
     * @param prefix The time prefix.
     */
    public void generateCharts(Prefix prefix) {
        generateCharts(prefix, true);
    }

    /**
     * Generate the charts using the given time prefix.
     *
     * @param prefix    The time prefix.
     * @param subCharts A boolean tag indicating if we want to generate sub chart ({@code true}) or not ({@code
     *                  false}).
     */
    public void generateCharts(Prefix prefix, boolean subCharts) {
        generateChart(title, benchmarks, subCharts, prefix);
    }

    /**
     * Compute the values of the benchmarks to get the optimal prefix for the results.
     *
     * @param benchmarks The benchmarks to compute.
     *
     * @return The Prefix for the results.
     */
    private Prefix computeOptimalPrefix(Collection<NamedBenchmark> benchmarks) {
        Prefix maxPrefix = null;

        for (NamedBenchmark benchmark : benchmarks) {
            BigDecimal mean = BigDecimal.valueOf(benchmark.getMean());

            Prefix prefix = Prefix.getScalePrefix(mean.doubleValue());

            maxPrefix = max(maxPrefix, prefix);
        }

        return maxPrefix;
    }

    /**
     * Generate the chart.
     *
     * @param title      The title of the chart.
     * @param benchmarks The benchmarks to include in the chart.
     * @param sub        A boolean tag indicating if we want to generate sub chart ({@code true}) or not ({@code
     *                   false}).
     * @param prefix     The time prefix.
     */
    private void generateChart(String title, Collection<NamedBenchmark> benchmarks, boolean sub, Prefix prefix) {
        String data = "Time (" + prefix.getSymbol() + "s)";

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (NamedBenchmark benchmark : benchmarks) {
            BigDecimal exactMean = BigDecimal.valueOf(benchmark.getMean());

            exactMean = exactMean.scaleByPowerOfTen(-prefix.getExponent());

            dataset.addValue(exactMean.doubleValue(), "", benchmark.getTitle());
        }

        JFreeChart chart = ChartFactory.createBarChart(title.replace("-sub", ""), "Methods", data, dataset, PlotOrientation.VERTICAL, false, false, false);

        final CategoryAxis domainAxis = chart.getCategoryPlot().getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        domainAxis.setMaximumCategoryLabelLines(5);

        BufferedImage image = chart.createBufferedImage(width, height);

        try {
            ImageIO.write(image, "png", new File(folder + title + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (sub) {
            generateSubChart(title, benchmarks);
        }
    }

    /**
     * Generate a sub chart from the benchmarks.
     *
     * @param title      The title of the chart.
     * @param benchmarks The benchmarks of the base chart.
     */
    private void generateSubChart(String title, Collection<NamedBenchmark> benchmarks) {
        List<NamedBenchmark> benchs = new ArrayList<NamedBenchmark>();

        for (NamedBenchmark benchmark : benchmarks) {
            double mean = benchmark.getMean();
            boolean high = false;

            for (NamedBenchmark other : benchmarks) {
                if (mean > other.getMean() * factor) {
                    high = true;
                    break;
                }
            }

            if (!high) {
                benchs.add(benchmark);
            }
        }

        if (benchs.size() < benchmarks.size() && benchs.size() > 1) {
            generateChart(title + "-sub", benchs, false, computeOptimalPrefix(benchs));
        }
    }

    /**
     * Return the max prefix between the two prefixes.
     *
     * @param prefix1 The first prefix.
     * @param prefix2 The second prefix.
     *
     * @return The max prefix.
     */
    private static Prefix max(Prefix prefix1, Prefix prefix2) {
        if(prefix1 == null){
            return prefix2;
        }

        if(prefix2.getExponent() > prefix1.getExponent()){
            return prefix2;
        }

        return prefix1;
    }

    /**
     * A named Benchmark.
     *
     * @author Baptiste Wicht
     */
    private static final class NamedBenchmark {
        private final Benchmark benchmark;
        private final String title;

        /**
         * Construct a named benchmark.
         *
         * @param benchmark The benchmark.
         * @param title The name of the benchmark.
         */
        private NamedBenchmark(Benchmark benchmark, String title) {
            super();

            this.benchmark = benchmark;
            this.title = title;
        }

        /**
         * Return the title of the benchmark.
         *
         * @return The title of the benchmark.
         */
        private String getTitle() {
            return title;
        }

        /**
         * Return the mean of the benchmark.
         *
         * @return The mean of the benchmark. 
         */
        private double getMean() {
            return benchmark.getMean();
        }
    }
}