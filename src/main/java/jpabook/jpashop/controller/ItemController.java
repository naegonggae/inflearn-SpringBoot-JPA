package jpabook.jpashop.controller;

import java.util.List;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class ItemController {

	private final ItemService itemService;

	@GetMapping("/items/new")
	public String createForm(Model model) {
		model.addAttribute("form", new BookForm());
		return "items/createItemForm";
	}

	@PostMapping("/items/new")
	public String create(BookForm form) {
		Book book = new Book();
		book.setName(form.getName());
		book.setPrice(form.getPrice());
		book.setStockQuantity(form.getStockQuantity());
		book.setAuthor(form.getAuthor());
		book.setIsbn(form.getIsbn());

		itemService.saveItem(book);
		return "redirect:/";
	}

	@GetMapping("/items")
	public String list(Model model) {
		List<Item> items = itemService.findItems();
		model.addAttribute("items", items);
		return "items/itemList";
	}

	// 수정하기 전에 전 데이터 불러오는 기능
	@GetMapping("/items/{itemId}/edit")
	public String updateItemForm(@PathVariable("itemId") Long itemId, Model model) {
		Book item = (Book) itemService.findOne(itemId);

		BookForm form = new BookForm();
		form.setId(item.getId());
		form.setName(item.getName());
		form.setPrice(item.getPrice());
		form.setStockQuantity(item.getStockQuantity());
		form.setAuthor(item.getAuthor());
		form.setIsbn(item.getIsbn());

		model.addAttribute("form", form);
		return "items/updateItemForm";
	}

	@PostMapping("/items/{itemId}/edit")
	public String updateItem(@ModelAttribute("form") BookForm form, @PathVariable Long itemId) {

		// 이것들은 실제로 DB에 존재하는 아이디로 Book 객체를 하나 더 만든것이다.
		// 존재하는 아이디로 객체를 만들었으니 JPA 가 commit 하기전에 변경감지를해서 DB의 아이디 객체에 update 를 해주나? 아니다.
		// 지금 만든 Book 객체는 JPA 가 인지하지 못하는 영속상태에 있지 않기 때문이다.
		// 그래서 변경감지를 하게끔 시키려면 새로만든 Book 객체를 영속상태로 만들어줘야한다. 방법은 find 로 찾아오고 나머지는 form 값을 입혀준다.
//		Book book = new Book();
//		book.setId(form.getId()); // id 에 접근할 수 있는지 권한 체크가 필요하다
//		book.setName(form.getName());
//		book.setPrice(form.getPrice());
//		book.setStockQuantity(form.getStockQuantity());
//		book.setAuthor(form.getAuthor());
//		book.setIsbn(form.getIsbn());
//		itemService.saveItem(book);

		// 병합을 쓰지말아야하는 이유
		// 병합은 변경감지와 다르게 통으로 수정하는 개념이다.
		// 그래서 만약 가격은 한번 정하면 불변이다해서 수정할때 제외를 해주고 하다보면 null 값이 들어가게 된다...
		// 병합으로 실무에서 깔끔하게 갈아끼우기 쉽지않다.
		// 그래서 변경감지가 좋다.

		// 세개만 바꾼다고 가정
		itemService.updateItem(itemId, form.getName(), form.getPrice(), form.getStockQuantity());

		return "redirect:/items";
	}

}
