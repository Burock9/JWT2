package com.burock.jwt_2.model;

public enum OrderStatus {
    PENDING, // BEKLEMEDE
    CONFIRMED, // ONAYLANDI
    PROCESSING, // İŞLENİYOR
    SHIPPED, // KARGOYA VERİLDİ
    DELIVERED, // TESLİM EDİLDİ
    CANCELLED // İPTAL EDİLDİ
}
