package org.processmining.eigenvalue.data;

import com.google.common.base.Joiner;

import java.util.Locale;

/**
 * Entropy-based precision measure for process mining.
 * It is computed by computing the topological entropy (the log of the largest eigenvalue)
 * @author andreas solti
 */
public class EntropyPrecisionRecall {

    private static Locale locale = Locale.ENGLISH;

    protected EntropyResult modelResult;
    protected EntropyResult logModelResult;
    protected EntropyResult logResult;

    protected long totalTime;
    public double percentFitting;

    public EntropyPrecisionRecall(EntropyResult logModelResult, EntropyResult modelResult, EntropyResult logResult, double percentFitting) {
        this.modelResult = modelResult;
        this.logModelResult = logModelResult;
        this.logResult = logResult;

        this.percentFitting = percentFitting;
    }

    public double getPrecision(){
        return getPrecision(PrecisionRecallStyle.EIGENVALUE);
    }

    public double getRecall(){
        return getRecall(PrecisionRecallStyle.EIGENVALUE);
    }

    public double getPrecision(PrecisionRecallStyle style) {
        return getMeasure(logModelResult.largestEigenvalue, modelResult.largestEigenvalue, style);
    }

    public double getRecall(PrecisionRecallStyle style) {
        return getMeasure(logModelResult.largestEigenvalue, logResult.largestEigenvalue, style);
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    public EntropyResult getModelResult() {
        return modelResult;
    }

    public EntropyResult getLogModelResult() {
        return logModelResult;
    }

    public EntropyResult getLogResult() {
        return logResult;
    }

    /**
     * We define precision simply as:
     * the (topological) size intersection of the log and the model
     *              ------    divided by   -------
     * the (topological) size of the model
     *
     * @param measureIntersection double
     * @param measureDenominator
     * @param style
     * @return
     */
    public static double getMeasure(double measureIntersection, double measureDenominator, PrecisionRecallStyle style) {
          if (measureIntersection <= 0){
              return 0;
          }
          if (measureDenominator <= 0){
              throw new IllegalStateException("How can the model have no positive measure, when the intersection with the log is positive?");
          }
          return style.applyTransformation(measureIntersection) / style.applyTransformation(measureDenominator);
    }

    public String prettyPrint(){
        StringBuilder builder = new StringBuilder();
        builder.append("<html><body>")
                .append(String.format(locale, "<h2>Precision: %.2f, Fitness: %.2f", getPrecision(), getRecall()))
                .append(String.format(locale, " - (completely fitting traces: %.2f%%) </h2>", percentFitting*100))
                .append("<p>eig(L &cap; M): ").append(logModelResult.largestEigenvalue).append("</p>")
                .append("<p>eig(M): ").append(modelResult.largestEigenvalue).append("</p>")
                .append("<p>eig(L): ").append(logResult.largestEigenvalue).append("</p><br/>")
                .append("<table>")
                .append("<tr><th>Details:</th><th>").append(Joiner.on("</th><th>").join(EntropyResult.getHeader().split(";"))).append("</th></tr>")
                .append("<tr><td>L &cap; M:</td><td>").append(Joiner.on("</td><td>").join(logModelResult.resultString().split(";"))).append("</td></tr>")
                .append("<tr><td>M:</td><td>").append(Joiner.on("</td><td>").join(modelResult.resultString().split(";"))).append("</td></tr>")
                .append("<tr><td>L:</td><td>").append(Joiner.on("</td><td>").join(logResult.resultString().split(";"))).append("</td></tr>")
                .append("</table>")
                .append("</body></html>");
        return builder.toString();
    }

    public String getCSVString(){
        return modelResult.resultString()+EntropyResult.SEPARATOR+logModelResult.resultString()+EntropyResult.SEPARATOR+logResult.resultString();
    }

    public static String getHeader() {
        String[] headerParts = EntropyResult.getHeader().split(EntropyResult.SEPARATOR);
        String model = "model_";
        String logModel = "logModel_";
        String log = "log_";
        return model+ Joiner.on(EntropyResult.SEPARATOR+model).join(headerParts)+EntropyResult.SEPARATOR+
                logModel+ Joiner.on(EntropyResult.SEPARATOR+logModel).join(headerParts)+EntropyResult.SEPARATOR+
                log+ Joiner.on(EntropyResult.SEPARATOR+log).join(headerParts);
    }
}
