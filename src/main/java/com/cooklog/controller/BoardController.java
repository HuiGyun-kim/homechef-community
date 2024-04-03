package com.cooklog.controller;

import com.cooklog.dto.BoardCreateRequestDTO;
import com.cooklog.dto.BoardDTO;
import com.cooklog.dto.BoardUpdateRequestDTO;
import com.cooklog.dto.CommentDTO;
import com.cooklog.dto.UserDTO;
import com.cooklog.exception.user.NotValidateUserException;
import com.cooklog.model.Board;
import com.cooklog.service.BoardService;
import com.cooklog.service.CommentService;
import com.cooklog.service.CustomIUserDetailsService;
import com.cooklog.validate.BoardCreateValidator;
import com.cooklog.validate.BoardUpdateValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final CommentService commentService;
    private final CustomIUserDetailsService userDetailsService;

    @GetMapping("/{id}")
    public String getBoard(@PathVariable Long id, Model model) {
        UserDTO userDTO = userDetailsService.getCurrentUserDTO();

        //조회수 업데이트
        boardService.updateReadCnt(id);

        BoardDTO board = boardService.getBoard(id, userDTO.getIdx());

        model.addAttribute("currentLoginUser", userDTO);
        model.addAttribute("board", board);

        return "board/board";
    }

    @GetMapping("/write")
    public String getWriteForm(Model model) {
        UserDTO userDTO = userDetailsService.getCurrentUserDTO();

        model.addAttribute("currentLoginUser", userDTO);
        model.addAttribute("board", new BoardCreateRequestDTO());

        return "board/boardForm";
    }

    @PostMapping("/write")
    public ResponseEntity<?> save(BoardCreateRequestDTO boardCreateRequestDTO,
                                  @RequestPart("images") List<MultipartFile> images,
                                  BindingResult result) throws IOException {

        // 현재 인증된(로그인한) 사용자의 idx 가져와서 userDTO에 할당
        UserDTO userDTO = userDetailsService.getCurrentUserDTO();

        //저장할 요소들 범위 넘지 않는지 확인하는 validate
        BoardCreateValidator boardCreateValidator = new BoardCreateValidator();
        boardCreateValidator.validate(boardCreateRequestDTO, result);

        Board board = boardService.save(userDTO.getIdx(), boardCreateRequestDTO, images);

        return ResponseEntity.ok("/board/" + board.getId());
    }

    @GetMapping("/edit/{id}")
    public String getEditForm(@PathVariable Long id, Model model) {

        UserDTO userDTO = userDetailsService.getCurrentUserDTO();

        BoardDTO board = boardService.getBoard(id, userDTO.getIdx());

        //만약 인증되지 않은 사용자라면 404페이지 리턴
        if (!board.getUserId().equals(userDTO.getIdx())) {
            return "error/404";
        }

        model.addAttribute("currentLoginUser", userDTO);
        model.addAttribute("board", board);

        return "board/boardEditForm";
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<?> edit(@PathVariable Long id, BoardUpdateRequestDTO boardUpdateRequestDTO,
                                  @RequestPart(value = "images", required = false) List<MultipartFile> images,
                                  BindingResult result) throws IOException {

        UserDTO userDTO = userDetailsService.getCurrentUserDTO();

        //만약 인증되지 않은 사용자라면 예외처리
        if (!boardUpdateRequestDTO.getUserId().equals(userDTO.getIdx())) {
            throw new NotValidateUserException();
        }

        //저장할 요소들 범위 넘지 않는지 확인하는 validate
        BoardUpdateValidator boardUpdateValidator=new BoardUpdateValidator();
        boardUpdateValidator.validate(boardUpdateRequestDTO, result);

        Board board = boardService.updateBoard(id, boardUpdateRequestDTO, boardUpdateRequestDTO.getImageUrls(), images);

        return ResponseEntity.ok("/board/" + id);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, @RequestParam("userId") Long userId) {
        UserDTO userDTO = userDetailsService.getCurrentUserDTO();

        //만약 인증되지 않은 사용자라면 예외처리
        if (!userId.equals(userDTO.getIdx())) {
            throw new NotValidateUserException();
        }

        boardService.deleteBoard(id);
        return ResponseEntity.ok("/");
    }

    //댓글 추가
    @PostMapping("/{boardId}/comments")
    public ResponseEntity<CommentDTO> addComment(@PathVariable Long boardId, @RequestBody CommentDTO commentDTO) {
        CommentDTO savedComment = commentService.addComment(boardId, commentDTO);
        return ResponseEntity.ok(savedComment);
    }

    // 댓글 수정
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentDTO> updateComment(@PathVariable Long commentId, @RequestBody CommentDTO commentDTO) {
        CommentDTO updatedComment = commentService.updateComment(commentId, commentDTO);
        if (updatedComment != null) {
            return ResponseEntity.ok(updatedComment);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    //댓글 삭제
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{boardId}/comments")
    public ResponseEntity<?> getComments(
            @PathVariable Long boardId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int limit) {

        Page<CommentDTO> commentPage = commentService.getCommentsByBoardId(boardId, page, limit);
        return ResponseEntity.ok(commentPage);
    }

    @GetMapping("/{parentId}/replies")
    public ResponseEntity<List<CommentDTO>> getRepliesByCommentId(
            @PathVariable Long parentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        List<CommentDTO> replies = commentService.getRepliesByCommentId(parentId, page, size);
        return ResponseEntity.ok(replies);
    }
}

