package com.imraan.fraudcheck.entity;

import java.util.Set;

public record Evaluation(int score, Set<String> triggeredRules) {
    }