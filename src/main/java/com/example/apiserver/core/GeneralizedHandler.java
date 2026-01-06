package com.example.apiserver.core;

import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function10;
import kotlin.jvm.functions.Function11;
import kotlin.jvm.functions.Function12;
import kotlin.jvm.functions.Function13;
import kotlin.jvm.functions.Function14;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.functions.Function3;
import kotlin.jvm.functions.Function4;
import kotlin.jvm.functions.Function5;
import kotlin.jvm.functions.Function6;
import kotlin.jvm.functions.Function7;
import kotlin.jvm.functions.Function8;
import kotlin.jvm.functions.Function9;

import java.util.function.Function;

@SuppressWarnings("unchecked")
interface GeneralizedHandler<R> {
    R apply(Object... params);

    static <R, T1> GeneralizedHandler<R> generalize(final Function1<T1, R> func) {
        return params -> func.invoke((T1) params[0]);
    }

    static <R, T1, T2> GeneralizedHandler<R> generalize(final Function2<T1, T2, R> func) {
        return params -> func.invoke((T1) params[0], (T2) params[1]);
    }

    static <R, T1, T2, T3> GeneralizedHandler<R> generalize(final Function3<T1, T2, T3, R> func) {
        return params -> func.invoke((T1) params[0], (T2) params[1], (T3) params[2]);
    }

    static <R, T1, T2, T3, T4> GeneralizedHandler<R> generalize(final Function4<T1, T2, T3, T4, R> func) {
        return params -> func.invoke((T1) params[0], (T2) params[1], (T3) params[2], (T4) params[3]);
    }

    static <R, T1, T2, T3, T4, T5> GeneralizedHandler<R> generalize(final Function5<T1, T2, T3, T4, T5, R> func) {
        return params -> func.invoke((T1) params[0], (T2) params[1], (T3) params[2], (T4) params[3], (T5) params[4]);
    }

    static <R, T1, T2, T3, T4, T5, T6> GeneralizedHandler<R> generalize(final Function6<T1, T2, T3, T4, T5, T6, R> func) {
        return params -> func.invoke((T1) params[0], (T2) params[1], (T3) params[2], (T4) params[3], (T5) params[4], (T6) params[5]);
    }

    static <R, T1, T2, T3, T4, T5, T6, T7> GeneralizedHandler<R> generalize(final Function7<T1, T2, T3, T4, T5, T6, T7, R> func) {
        return params -> func.invoke((T1) params[0], (T2) params[1], (T3) params[2], (T4) params[3], (T5) params[4], (T6) params[5], (T7) params[6]);
    }

    static <R, T1, T2, T3, T4, T5, T6, T7, T8> GeneralizedHandler<R> generalize(final Function8<T1, T2, T3, T4, T5, T6, T7, T8, R> func) {
        return params -> func.invoke((T1) params[0], (T2) params[1], (T3) params[2], (T4) params[3], (T5) params[4], (T6) params[5], (T7) params[6], (T8) params[7]);
    }

    static <R, T1, T2, T3, T4, T5, T6, T7, T8, T9> GeneralizedHandler<R> generalize(final Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, R> func) {
        return params -> func.invoke((T1) params[0], (T2) params[1], (T3) params[2], (T4) params[3], (T5) params[4], (T6) params[5], (T7) params[6], (T8) params[7], (T9) params[8]);
    }

    static <R, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> GeneralizedHandler<R> generalize(final Function10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R> func) {
        return params -> func.invoke((T1) params[0], (T2) params[1], (T3) params[2], (T4) params[3], (T5) params[4], (T6) params[5], (T7) params[6], (T8) params[7], (T9) params[8], (T10) params[9]);
    }

    static <R, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> GeneralizedHandler<R> generalize(final Function11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R> func) {
        return params -> func.invoke((T1) params[0], (T2) params[1], (T3) params[2], (T4) params[3], (T5) params[4], (T6) params[5], (T7) params[6], (T8) params[7], (T9) params[8], (T10) params[9], (T11) params[10]);
    }

    static <R, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> GeneralizedHandler<R> generalize(final Function12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R> func) {
        return params -> func.invoke((T1) params[0], (T2) params[1], (T3) params[2], (T4) params[3], (T5) params[4], (T6) params[5], (T7) params[6], (T8) params[7], (T9) params[8], (T10) params[9], (T11) params[10], (T12) params[11]);
    }

    static <R, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> GeneralizedHandler<R> generalize(final Function13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R> func) {
        return params -> func.invoke((T1) params[0], (T2) params[1], (T3) params[2], (T4) params[3], (T5) params[4], (T6) params[5], (T7) params[6], (T8) params[7], (T9) params[8], (T10) params[9], (T11) params[10], (T12) params[11], (T13) params[12]);
    }

    static <R, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> GeneralizedHandler<R> generalize(final Function14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R> func) {
        return params -> func.invoke((T1) params[0], (T2) params[1], (T3) params[2], (T4) params[3], (T5) params[4], (T6) params[5], (T7) params[6], (T8) params[7], (T9) params[8], (T10) params[9], (T11) params[10], (T12) params[11], (T13) params[12], (T14) params[13]);
    }
}
