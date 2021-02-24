package com.enliple.pudding.bus

/**
 * Created by Kim Joonsung on 2019-04-18.
 */

/**
 *  상품 상세 - 상품 문의 탭 개수 가져오는 Bus
 */
class QnABus(var type: Int, var count: Int)

/**
 *  편성표 - 방송예약 내역 개수 가져오는 Bus
 */
class ReservationBus(var type: Int, var count: Int)

/**
 * 검색 결과 개수 가져오는 Bus
 */
class SearchResultBus(var type: Int, var count: Int)

/**
 * 영상 상품 리스트에서 찜을 눌렀을 경우 사용하는 Bus
 */
class ZzimStatusBus(var productId:String, var status:String, var count: String)

/**
* 영상에서 조회수 카운트에 사용하는 Bus
*/
class ViewCountBus(var count: Int, var streamKey: String, var position:Int)
