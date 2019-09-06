package org.spearhead.account.dto;

import lombok.*;
import org.spearhead.common.common.api.BaseResponse;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ListAccountResponse extends BaseResponse {
    private AccountList accountList;
}
