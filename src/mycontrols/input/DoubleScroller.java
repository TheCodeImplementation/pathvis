/*
 * Created by The Code Implementation, Mar 2020.
 * Youtube channel: https://www.youtube.com/channel/UCecfXH0CwYv-CA0Oo3-8PFg
 * Github: https://github.com/TheCodeImplementation
 */

package mycontrols.input;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.ImageCursor;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

public class DoubleScroller extends Group {

    private TextField       textField = new TextField();
    private DoubleProperty  minValue = new SimpleDoubleProperty();
    private DoubleProperty  maxValue = new SimpleDoubleProperty();
    private DoubleProperty  value = new SimpleDoubleProperty();
    private DoubleProperty  amountToStepBy = new SimpleDoubleProperty();
    private final String    potentialNumber = "^$|^[+-]?[\\d]*[.]?[\\d]*$";
    private final String    legalNumber = "^[+-]?(?:\\d+\\.?\\d*|\\d*\\.\\d+)$";
    private IntegerProperty decimalPrecision = new SimpleIntegerProperty();

    public DoubleScroller() {
        this(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0d, 1d);
    }
    public DoubleScroller(Double min, Double max, Double initialValue, Double amountToStepBy) {
        setMinValue(min);
        setMaxValue(max);
        setValue(initialValue);
        setAmountToStepBy(amountToStepBy);
        setText(parseString(getValue()));
        setDecimalPrecision(calcDecimalPrecision(getValue(), getAmountToStepBy()));
        initialize();
    }

    private void    initialize() {
        textField.getStyleClass().add("doubleScroller");
        getStylesheets().add(getClass().getResource("resources/Styles.css").toExternalForm());
        getChildren().add(textField);
        Image image = new Image(getClass().getResourceAsStream("resources/upDownArrow.png"));
        textField.setCursor(new ImageCursor(image, image.getWidth()/2,image.getHeight()/2));
        setWidth(80);
        decorate();
        //text is entered listener
        textProperty().addListener((l, o, n) -> {
            //if the value entered cannot become a legal number
            if (!n.matches(potentialNumber)) {
                //don't even allow it in the text box
                setText(o);
            } else {
                setValue(parseNumber(n));
            }
        });
        //textbox gains focus listener
        textField.focusedProperty().addListener((l, o, n) -> {
            if (n) {
                //when the box is clicked all the text will be highlighted. must be runLater to work.
                Platform.runLater(() -> {
                    textField.selectAll();
                });
            } else {
                setText(parseString(getValue()));
            }
        });
        //mouse scroll listener
        addEventHandler(ScrollEvent.SCROLL, e -> {
            //each scroll generates two event: one of them has a deltaY of 0, which is not needed.
            if (e.getDeltaY() != 0) {
                int x = calcSpinMultiplier(e.getX());
                setValue( getInBoundNum( getValue() + (e.getDeltaY() > 0 ? getAmountToStepBy()*x : -getAmountToStepBy()*x) ) );
                setText(parseString(getValue()));
            }
            e.consume();
        });
        //internal value listener
        valueProperty().addListener( (l,o,n) -> {
            if(!textField.isFocused()){
                setText(parseString(getValue()));
            }
        });
    }
    //Add the dividing tick marks to the control
    private void    decorate() {
        Region region = new Region();
        region.getStyleClass().add("doubleScrollerTickMarks");
        int regionHeight = 5;
        region.setPrefHeight(regionHeight);
        region.prefWidthProperty().bind(textField.widthProperty());
        region.layoutYProperty().bind(textField.heightProperty().add(-regionHeight));
        region.setDisable(true);
        SVGPath svg = new SVGPath();
        svg.setContent("M0 0L0 40L600 40L600 0    M200 0L200 40    M400 0L400 40");
        region.setShape(svg);
        getChildren().add(region);
    }
    private Double  parseNumber(String text) {
        if (!text.matches(legalNumber)) {
            return parseNumber("0.0");
        } else {
            return Double.parseDouble(text);
        }
    }
    private String  parseString(Double num){
        return String.format("%." + getDecimalPrecision() + "f", num);
    }
    private double  getInBoundNum(double num){
        if (num < getMinValue()) {
            playRedBlurAnimation(-1);
            return getMinValue();
        } else if (num > getMaxValue()) {
            playRedBlurAnimation(1);
            return getMaxValue();
        } else {
            return num;
        }
    }
    private int     calcDecimalPrecision(Double initialValue, Double amountToStepBy){
        //how many decimal places are in amountToStepBy and initialValue?
        String[] amountToStepByArray    = amountToStepBy.toString().split("[.]");
        String[] initialValueArray      = initialValue.toString().split("[.]");
        //which of the two has the most decimal places
        return Math.max( amountToStepByArray[amountToStepByArray.length - 1].length(),
                initialValueArray[initialValueArray.length - 1].length() );
    }
    private int     calcSpinMultiplier(double x){
        //which third of the box the scroll event happend in will affect the multiplier.
        double thirds = getBoundsInLocal().getWidth()/3;
        if(x < thirds){
            return 1;
        }else if(x < 2*thirds){
            return 10;
        }else{
            return 100;
        }
    }
    //Thanks to Stack Overflow user negste, for his help with this code: https://stackoverflow.com/a/41672541
    private void    playRedBlurAnimation(int posNeg){
        InnerShadow s = new InnerShadow();
        s.setWidth(0);
        s.setOffsetY(5*posNeg);
        textField.setEffect(s);

        final Animation animation = new Transition() {
            {
                setCycleDuration(Duration.millis(300));
                setInterpolator(Interpolator.EASE_OUT);
            }
            @Override
            protected void interpolate(double frac) {
                Color vColor = Color.color(1,.8,.8,1 - frac);
                s.setColor(vColor);
            }
        };
        animation.play();
        animation.setOnFinished( ev -> textField.setEffect(null));
    }

    public double                   getMinValue() {
        return minValue.get();
    }
    public DoubleProperty           minValueProperty() {
        return minValue;
    }
    public void                     setMinValue(double minValue) {
        this.minValue.set(minValue);
        if(getMinValue() > getMaxValue()){
            //max must be raised too in this case.
            setMaxValue(getMinValue());
        }
    }
    public double                   getMaxValue() {
        return maxValue.get();
    }
    public DoubleProperty           maxValueProperty() {
        return maxValue;
    }
    public void                     setMaxValue(double maxValue) {
        this.maxValue.set(maxValue);
        if(getMaxValue() < getMinValue()){
            //min must be lowered too in this case.
            setMinValue(maxValue);
        }
    }
    public double                   getValue() {
        return value.get();
    }
    public DoubleProperty           valueProperty() {
        return value;
    }
    public void                     setValue(double value) {
        this.value.set( getInBoundNum(value) );
    }
    public double                   getAmountToStepBy() {
        return amountToStepBy.get();
    }
    public DoubleProperty           amountToStepByProperty() {
        return amountToStepBy;
    }
    public void                     setAmountToStepBy(double amountToStepBy) {
        this.amountToStepBy.set(amountToStepBy);
    }
    public int                      getDecimalPrecision() {
        return decimalPrecision.get();
    }
    public IntegerProperty          decimalPrecisionProperty() {
        return decimalPrecision;
    }
    public void                     setDecimalPrecision(int decimalPrecision) {
        //decimal precision cannot be negative.
        this.decimalPrecision.set(decimalPrecision >= 0 ? decimalPrecision : 0);
    }
    public String                   getText() {
        return textField.getText();
    }
    public StringProperty           textProperty() {
        return textField.textProperty();
    }
    public void                     setText(String text) {
        this.textField.setText(text);
    }
    public void                     setHeight(double height){
        textField.setMinHeight(height);
        textField.setMaxHeight(height);
    }
    public void                     setWidth(double width){
        textField.setMinWidth(width);
        textField.setMaxWidth(width);
    }
}